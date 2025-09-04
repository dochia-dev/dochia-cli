package dev.dochia.cli.core.command;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ShortErrorMessageHandlerTest {

    private CommandLine mockCommandLine;
    private CommandLine.Model.CommandSpec mockCommandSpec;
    private CommandLine.IExitCodeExceptionMapper mockExitCodeMapper;

    private final StringWriter out = new StringWriter();
    private final StringWriter err = new StringWriter();
    private final ShortErrorMessageHandler handler = new ShortErrorMessageHandler();

    private CommandLine.Help.ColorScheme createColorScheme() {
        return CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.OFF);
    }

    @BeforeEach
    void setup() {
        mockCommandLine = Mockito.mock(CommandLine.class);
        mockCommandSpec = Mockito.mock(CommandLine.Model.CommandSpec.class);
        mockExitCodeMapper = Mockito.mock(CommandLine.IExitCodeExceptionMapper.class);
    }

    @Test
    void shouldPrintFullUsageAndReturnOkWhenHelpFullArgPresent() {
        when(mockCommandLine.getOut()).thenReturn(new PrintWriter(out));
        String[] args = {"--help-full"};
        CommandLine.ParameterException ex =
                new CommandLine.ParameterException(mockCommandLine, "test error");
        int exitCode = handler.handleParseException(ex, args);

        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        verify(mockCommandLine).usage(any(PrintWriter.class));
    }

    @Test
    void shouldPrintErrorAndSuggestionsWhenHelpFullNotPresent() {
        when(mockCommandLine.getErr()).thenReturn(new PrintWriter(err));
        when(mockCommandLine.getCommandSpec()).thenReturn(mockCommandSpec);
        when(mockCommandSpec.exitCodeOnInvalidInput()).thenReturn(CommandLine.ExitCode.USAGE);
        when(mockCommandLine.getColorScheme()).thenReturn(createColorScheme());
        String[] args = {};
        CommandLine.ParameterException ex =
                new CommandLine.ParameterException(mockCommandLine, "test error");

        int exitCode = handler.handleParseException(ex, args);

        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(err.toString()).contains("test error");
    }

    @Test
    void shouldUseMappedExitCodeWhenExitCodeMapperPresent() {
        when(mockCommandLine.getExitCodeExceptionMapper()).thenReturn(mockExitCodeMapper);
        when(mockCommandLine.getErr()).thenReturn(new PrintWriter(err));
        when(mockExitCodeMapper.getExitCode(any())).thenReturn(42);
        when(mockCommandLine.getColorScheme()).thenReturn(createColorScheme());
        String[] args = {};
        CommandLine.ParameterException ex =
                new CommandLine.ParameterException(mockCommandLine, "test error");

        int exitCode = handler.handleParseException(ex, args);

        assertThat(exitCode).isEqualTo(42);
        verify(mockExitCodeMapper).getExitCode(ex);
    }

    @Test
    void shouldUseDefaultExitCodeWhenNoExitCodeMapper() {
        when(mockCommandLine.getExitCodeExceptionMapper()).thenReturn(null);
        when(mockCommandLine.getErr()).thenReturn(new PrintWriter(err));
        when(mockCommandLine.getCommandSpec()).thenReturn(mockCommandSpec);
        when(mockCommandSpec.exitCodeOnInvalidInput()).thenReturn(123);
        when(mockCommandLine.getColorScheme()).thenReturn(createColorScheme());
        String[] args = {};
        CommandLine.ParameterException ex =
                new CommandLine.ParameterException(mockCommandLine, "test error");

        int exitCode = handler.handleParseException(ex, args);

        assertThat(exitCode).isEqualTo(123);
    }
}
