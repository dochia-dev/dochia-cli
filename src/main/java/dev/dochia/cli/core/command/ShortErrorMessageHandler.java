package dev.dochia.cli.core.command;

import dev.dochia.cli.core.util.ConsoleUtils;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Handles parameter exceptions by printing a short error message to the console, rather than full help.
 */
public class ShortErrorMessageHandler implements CommandLine.IParameterExceptionHandler {

    /**
     * Handles a {@link CommandLine.ParameterException} by printing a short error message to the console.
     *
     * @param ex   the exception that was thrown
     * @param args the command line arguments that were parsed when the exception was thrown
     * @return the exit code to return to the operating system
     */
    @Override
    public int handleParseException(CommandLine.ParameterException ex, String[] args) {
        CommandLine cmd = ex.getCommandLine();

        if (Arrays.asList(args).contains("--help-full")) {
            cmd.usage(cmd.getOut());
            return CommandLine.ExitCode.OK;
        }

        PrintWriter err = cmd.getErr();

        err.println(cmd.getColorScheme().errorText(ex.getMessage())); // bold red
        CommandLine.UnmatchedArgumentException.printSuggestions(ex, err);
        ConsoleUtils.emptyLine();
        err.println(ConsoleUtils.SHORT_HELP);

        CommandLine.Model.CommandSpec spec = cmd.getCommandSpec();

        return cmd.getExitCodeExceptionMapper() != null
                ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                : spec.exitCodeOnInvalidInput();
    }
}
