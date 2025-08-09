package dev.dochia.cli.core.command;

import dev.dochia.cli.core.command.model.HelpFullOption;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.VersionProvider;
import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;
import picocli.AutoComplete;
import picocli.CommandLine;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@CommandLine.Command(
        name = "dochia",
        mixinStandardHelpOptions = true,
        header = {
                "%n@|green dochia automatically generates and executes negative and boundary testing so you can focus on creative problem-solving; version ${app.version}|@ %n",
                """
                             _             _     _      \s
                            | |           | |   (_)     \s
                          __| | ___   ____| |__  _ _____\s
                         / _  |/ _ \\ / ___)  _ \\| (____ |
                        ( (_| | |_| ( (___| | | | / ___ |
                         \\____|\\___/ \\____)_| |_|_\\_____|
                        
                                Bringing Chaos with Love!
                        """
        },
        usageHelpAutoWidth = true,
        versionProvider = VersionProvider.class,
        commandListHeading = "%n@|bold,underline Commands:|@%n",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        exitCodeOnInvalidInput = 191,
        exitCodeOnExecutionException = 192,
        resourceBundle = "version",
        abbreviateSynopsis = true,
        synopsisHeading = "@|bold,underline Usage:|@%n",
        customSynopsis = {
                "@|bold dochia|@ @|fg(yellow) test -c|@ <contract> @|fg(yellow) -s|@ <server> [ADDITIONAL OPTIONS]",
                "@|bold dochia (test | list | replay | info | stats | random | explain)|@ [OPTIONS]"
        },
        exitCodeListHeading = "%n@|bold,underline Exit Codes:|@%n",
        exitCodeList = {
                "@|bold  0|@:Successful program execution",
                "@|bold 191|@:Usage error: user input for the command was incorrect",
                "@|bold 192|@:Internal execution error: an exception occurred when executing command",
                "@|bold ERR|@:Where ERR is the number of errors reported by dochia"
        },
        footerHeading = "%n@|bold,underline Examples:|@%n",
        footer = {
                "  Run in blackbox mode and only report 500 http error codes:",
                "    dochia test -c openapi.yml -s http://localhost:8080 -b -k",
                "",
                "  Run with authentication headers from an environment variable called TOKEN:",
                "    dochia test -c openapi.yml -s http://localhost:8080 -H API-Token=$$TOKEN"
        },
        subcommands = {
                AutoComplete.GenerateCompletion.class,
                CommandLine.HelpCommand.class,
                TestCommand.class,
                ListCommand.class,
                ReplayCommand.class,
                InfoCommand.class,
                RandomCommand.class,
                ExplainCommand.class
        })
@Unremovable
@Singleton
public class DochiaCommand implements Runnable, CommandLine.IExitCodeGenerator {
    @CommandLine.Mixin
    HelpFullOption helpFullOption;

    @CommandLine.Option(names = "--licenses", description = "Show all third party licenses used by the dochia CLI")
    boolean licenses;

    @Override
    public void run() {
        if (licenses) {
            System.exit(displayLicenses());
        }
        System.out.println(ConsoleUtils.getShortVersionOfHelp());
    }

    private int displayLicenses() {
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("THIRD_PARTY_LICENSES.txt")) {
            if (in == null) {
                System.err.println("License file not found.");
                return 1;
            }
            System.out.println(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            return 0;
        } catch (Exception e) {
            System.err.println("Error reading license file: " + e.getMessage());
            return 1;
        }
    }

    @Override
    public int getExitCode() {
        return 0;
    }
}
