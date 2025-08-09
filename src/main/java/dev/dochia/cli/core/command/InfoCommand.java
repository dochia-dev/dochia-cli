package dev.dochia.cli.core.command;

import dev.dochia.cli.core.command.model.HelpFullOption;
import dev.dochia.cli.core.util.JsonUtils;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.VersionProvider;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.quarkus.arc.Unremovable;
import io.quarkus.bootstrap.graal.ImageInfo;
import jakarta.inject.Inject;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import picocli.CommandLine;

/**
 * Display info relate to the environment where dochia is running.
 */
@CommandLine.Command(
        name = "info",
        mixinStandardHelpOptions = true,
        usageHelpAutoWidth = true,
        exitCodeOnInvalidInput = 191,
        exitCodeOnExecutionException = 192,
        exitCodeListHeading = "%n@|bold,underline Exit Codes:|@%n",
        exitCodeList = {"@|bold  0|@:Successful program execution",
                "@|bold 191|@:Usage error: user input for the command was incorrect",
                "@|bold 192|@:Internal execution error: an exception occurred when executing command"},
        footerHeading = "%n@|bold,underline Examples:|@%n",
        footer = {"  Get info about current environment:",
                "    dochia info",
                "", "  Get info about current environment in json format:",
                "    dochia info -j"},
        description = "Get environment debug info to help in bug reports.",
        versionProvider = VersionProvider.class)
@Unremovable
public class InfoCommand implements Runnable {
    private final PrettyLogger logger = PrettyLoggerFactory.getConsoleLogger();
    @CommandLine.Option(names = {"-j", "--json"},
            description = "Output to console in JSON format.")
    private boolean json;

    @CommandLine.Mixin
    HelpFullOption helpFullOption;

    @Getter
    @ConfigProperty(name = "quarkus.application.version", defaultValue = "1.0.0")
    String appVersion;
    @ConfigProperty(name = "app.timestamp", defaultValue = "1-1-1")
    String appBuildTime;

    @Inject
    TestCommand testCommand;

    @Override
    public void run() {
        ConsoleUtils.initTerminalWidth(testCommand.spec);
        String imageType = ImageInfo.inImageRuntimeCode() ? "native" : "uber-jar";
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        String terminalWidth = String.valueOf(ConsoleUtils.getTerminalWidth());
        String terminalType = ConsoleUtils.getTerminalType();
        String shell = ConsoleUtils.getShell();

        EnvInfo envInfo = new EnvInfo(osName, osVersion, osArch, appVersion, imageType, appBuildTime, terminalWidth, terminalType, shell);

        if (json) {
            displayJson(envInfo);
        } else {
            displayText(envInfo);
        }

    }

    void displayJson(EnvInfo envInfo) {
        logger.noFormat(JsonUtils.GSON.toJson(envInfo));
    }

    void displayText(EnvInfo envInfo) {
        logger.noFormat("Key             | Value");
        logger.noFormat("--------------- | --------------------");
        logger.noFormat("OS Name         | " + envInfo.osName);
        logger.noFormat("OS Version      | " + envInfo.osVersion);
        logger.noFormat("OS Arch         | " + envInfo.osArch);
        logger.noFormat("Binary Type     | " + envInfo.imageType);
        logger.noFormat("Dochia Version  | " + envInfo.version);
        logger.noFormat("Dochia Build    | " + envInfo.buildTime);
        logger.noFormat("Term Width      | " + envInfo.terminalWidth);
        logger.noFormat("Term Type       | " + envInfo.terminalType);
        logger.noFormat("Shell           | " + envInfo.shell);
    }

    /**
     * A record representing information about dochia, including details about the operating system,
     * Dochia version, image type, build time, terminal properties, and shell information.
     *
     * @param osName        the name of the operating system
     * @param osVersion     the version of the operating system
     * @param osArch        the architecture of the operating system
     * @param version       the version of Dochia
     * @param imageType     the type of the Dochia image
     * @param buildTime     the build time of the Dochia image
     * @param terminalWidth the width of the terminal
     * @param terminalType  the type of the terminal
     * @param shell         the shell used by dochia
     */
    public record EnvInfo(String osName, String osVersion, String osArch, String version, String imageType,
                          String buildTime, String terminalWidth, String terminalType, String shell) {
    }
}
