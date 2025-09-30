package dev.dochia.cli.core;

import dev.dochia.cli.core.command.DochiaCommand;
import dev.dochia.cli.core.command.ShortErrorMessageHandler;
import dev.dochia.cli.core.exception.DochiaException;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * Main application entry point.
 */
@QuarkusMain
public class DochiaMain implements QuarkusApplication {

    @Inject
    DochiaCommand dochiaCommand;

    @Inject
    CommandLine.IFactory factory;

    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(DochiaMain.class);

    static void main(String[] args) {
        checkForSummaryReport(args);

        Quarkus.run(DochiaMain.class, args);
    }

    @Override
    public int run(String... args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logger.fatal("Something unexpected happened: {}", e.getMessage());
            logger.debug("Stacktrace", e);
        });
        checkForConsoleColorsDisabled(args);

        CommandLine commandLine = new CommandLine(dochiaCommand, factory)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setColorScheme(colorScheme())
                .setAbbreviatedOptionsAllowed(true)
                .setAbbreviatedSubcommandsAllowed(true);
        commandLine.getCommandSpec().usageMessage().abbreviateSynopsis(true);
        commandLine.setExecutionStrategy(parseResult -> {
            if (commandLine.isUsageHelpRequested() || (parseResult.subcommand() != null && parseResult.subcommand().isUsageHelpRequested())) {
                commandLine.getOut().println(ConsoleUtils.SHORT_HELP);
                return CommandLine.ExitCode.OK;
            }
            if (parseResult.subcommand() != null && parseResult.subcommand().hasMatchedOption("--help-full")) {
                parseResult.subcommand().commandSpec().commandLine().usage(commandLine.getOut());
                return CommandLine.ExitCode.OK;
            }
            if (parseResult.hasMatchedOption("--help-full")) {
                commandLine.usage(commandLine.getOut());
                return CommandLine.ExitCode.OK;
            }
            return new CommandLine.RunLast().execute(parseResult);
        });
        loadConfigIfSupplied(commandLine, args);
        return commandLine.setParameterExceptionHandler(new ShortErrorMessageHandler()).execute(args);
    }

    private static void checkForSummaryReport(String[] args) {
        boolean verbose = Arrays.asList(args).contains("-vv") || Arrays.asList(args).contains("-vvv");
        if (verbose) {
            System.setProperty("quarkus.log.console.format", "[%X{id_ansi}][%X{playbook}] %m %n");
        }
    }

    private void loadConfigIfSupplied(CommandLine commandLine, String... args) {
        File configFile = findConfigFile(args);
        if (configFile != null && configFile.exists()) {
            logger.config("Loading config from {}", configFile.getAbsolutePath());
            Properties props = new Properties();
            try (InputStream in = new FileInputStream(configFile)) {
                props.load(in);
                commandLine.setDefaultValueProvider(new CommandLine.PropertiesDefaultProvider(props));
            } catch (Exception e) {
                throw new DochiaException(e);
            }
        }
    }

    private void checkForConsoleColorsDisabled(String... args) {
        boolean colorsDisabled = Arrays.asList(args).contains("--no-color");
        if (colorsDisabled) {
            PrettyLogger.disableColors();
            PrettyLogger.changeMessageFormat("%1$-12s");
        }
    }

    CommandLine.Help.ColorScheme colorScheme() {
        return new CommandLine.Help.ColorScheme.Builder()
                .commands(CommandLine.Help.Ansi.Style.bold)
                .options(CommandLine.Help.Ansi.Style.fg_yellow)
                .parameters(CommandLine.Help.Ansi.Style.fg_yellow)
                .optionParams(CommandLine.Help.Ansi.Style.faint)
                .errors(CommandLine.Help.Ansi.Style.fg_red, CommandLine.Help.Ansi.Style.bold)
                .stackTraces(CommandLine.Help.Ansi.Style.italic)
                .build();
    }

    private File findConfigFile(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--config".equals(args[i])) {
                return new File(args[i + 1]);
            } else if (args[i].startsWith("--config=")) {
                return new File(args[i].split("=", 2)[1]);
            }
        }
        return null;
    }
}