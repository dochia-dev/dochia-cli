package dev.dochia.cli.core.util;

import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;
import picocli.CommandLine;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Utility class for console-related operations.
 */
public abstract class ConsoleUtils {
    private static final String QUARKUS_PROXY_SUFFIX = "_Subclass";
    private static final String REGEX_TO_REMOVE_FROM_PLAYBOOK_NAMES = "TrimValidate|ValidateTrim|SanitizeValidate|ValidateSanitize|Playbook";

    private static final PrettyLogger LOGGER = PrettyLoggerFactory.getConsoleLogger();

    private static final Pattern ANSI_REMOVE_PATTERN = Pattern.compile("\u001B\\[[;\\d]*m");
    public static final String SEPARATOR = "  ";

    /**
     * Get the width of the terminal.
     * <p>
     * Size is cached, so it won't reach to width changes during run.
     */
    @Getter
    private static int terminalWidth = 80;

    private ConsoleUtils() {
        //ntd
    }

    /**
     * Initialize the terminal width.
     *
     * @param spec The command spec.
     */
    public static void initTerminalWidth(CommandLine.Model.CommandSpec spec) {
        try {
            terminalWidth = Math.min(140, spec.usageMessage().width());
        } catch (Exception e) {
            terminalWidth = 80;
        }
    }

    /**
     * Center a string with ANSI color, applying padding and using the specified color.
     *
     * @param str     The input string.
     * @param padding The padding size.
     * @param color   The ANSI color to apply.
     * @return The centered string with ANSI color.
     */
    public static String centerWithAnsiColor(String str, int padding, Ansi.Color color) {
        String strAnsi = Ansi.ansi().fg(color).bold().a(str).reset().toString();
        int paddingLength = strAnsi.length() - str.length() + padding;
        return StringUtils.center(strAnsi, paddingLength, "*");
    }

    /**
     * Sanitize a playbook name by removing a specific suffix.
     *
     * @param currentName The current playbook name.
     * @return The sanitized playbook name.
     */
    public static String sanitizePlaybookName(String currentName) {
        return currentName.replace(QUARKUS_PROXY_SUFFIX, "");
    }

    /**
     * Remove specific substrings from a playbook name based on a regular expression pattern.
     *
     * @param currentName The current playbook name.
     * @return The modified playbook name.
     */
    public static String removeTrimSanitize(String currentName) {
        return currentName.replaceAll(REGEX_TO_REMOVE_FROM_PLAYBOOK_NAMES, "");
    }

    /**
     * Get the type of the terminal.
     *
     * @return The terminal type or "unknown" if unable to determine.
     */
    public static String getTerminalType() {
        String terminalType = System.getenv("TERM");
        if (terminalType != null && !terminalType.isEmpty()) {
            return terminalType;
        }

        return "unknown";
    }

    /**
     * Get the user's shell environment variable.
     *
     * @return The shell or "unknown" if the environment variable is not set.
     */
    public static String getShell() {
        return Optional.ofNullable(System.getenv("SHELL")).orElse("unknown");
    }

    /**
     * Render a progress row on the same console row.
     *
     * @param path     The path being processed.
     * @param progress The progress character to be displayed.
     */
    public static void renderSameRow(String path, char progress) {
        renderRow("\r", path, progress);
    }

    /**
     * Render a progress row on a new console row.
     *
     * @param path     The path being processed.
     * @param progress The progress character to be displayed.
     */
    public static void renderNewRow(String path, char progress) {
        renderRow(System.lineSeparator(), path, progress);
    }

    /**
     * Render a progress row with a specific prefix.
     *
     * @param prefix The prefix for the progress row.
     * @param path   The path being processed.
     */
    public static void renderRow(String prefix, String path, char progressChar) {
        String withoutAnsi = ANSI_REMOVE_PATTERN.matcher(path).replaceAll("");
        int dots = Math.max(terminalWidth - withoutAnsi.length() - 2, 1);
        String firstPart = " ".repeat(3) + path.substring(0, path.indexOf(SEPARATOR));
        String secondPart = path.substring(path.indexOf(SEPARATOR) + 1);
        String toPrint = Ansi.ansi().bold().a(prefix + firstPart + " " + ".".repeat(dots) + secondPart + " " + progressChar).reset().toString();

        //we just use system.out as the logger adds a new line
        System.out.print(toPrint);
    }

    /**
     * Get the number of console columns, subtracting a specified value.
     *
     * @param toSubtract The value to subtract from the total number of columns.
     * @return The adjusted number of columns.
     */
    public static int getConsoleColumns(int toSubtract) {
        return terminalWidth - toSubtract;
    }

    /**
     * Render a header in the console with surrounding equals signs.
     *
     * @param header The header text to be rendered.
     */
    public static void renderHeader(String header) {
        System.out.print("\uD83E\uDDEA " + Ansi.ansi().bold().a(header).reset().toString());
    }

    /**
     * Prints the given message to the console on the same row and then moves the cursor to a new line.
     *
     * @param message the message
     */
    public static void renderSameRowAndMoveToNextLine(String message) {
        int spacesToAdd = Math.max(getConsoleColumns(message.length()), 0);

        //we just use system.out as the logger adds a new line
        System.out.print("\r" + message + " ".repeat(spacesToAdd) + "\n");
    }

    /**
     * Print an empty line to the console.
     */
    public static void emptyLine() {
        LOGGER.noFormat(" ");
    }

    public static String getShortVersionOfHelp() {
        return """
                dochia – Bringing chaos with love!
                
                Description:
                  dochia automatically generates and executes negative and boundary testing
                  so you can focus on creative problem-solving.
                
                  dochia is designed to work with OpenAPI specifications and can be run in various modes.
                  The simplest way to run dochia is to provide the OpenAPI spec and the server URL.
                
                Examples:
                  dochia test -c api.yml -s http://localhost:8080 -H 'Authorization=Bearer TOKEN'
                
                Usage:
                  dochia test -c <contract> -s <server> [OPTIONS]
                  dochia (test | fuzz | replay | list | info | explain) [OPTIONS]
                
                Common Options:
                  -h, --help                     Show help and exit.
                  -V, --version                  Print version and exit.
                  -c, --contract=<contract>      API contract or spec.
                  -s, --server=<server>          Base URL of the service.
                  -H=<name=value>                Custom headers applicable to all paths(e.g., auth tokens)
                      --headers=<file>           YAML file with custom headers specific per path (e.g., auth tokens).
                  -R=<name=value>                Fixed field values applicable to all paths.
                      --reference-data=<file>          YAML file with fixed field values specific per path.
                  -f, --playbooks=<list>           Playbook names to run.
                  -p, --paths=<list>             Specific paths to test.
                  -d, --dry-run                  Simulate tests without executing.
                
                For full commands and options, run:
                  dochia (test | fuzz | replay | list | info | explain) --help-full
                
                """;
    }
}
