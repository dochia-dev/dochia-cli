package dev.dochia.cli.core.command;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@QuarkusTest
class ShortErrorMessageHandlerTest {

    @Mock
    private CommandLine mockCommandLine;
    @Mock
    private CommandLine.Model.CommandSpec mockCommandSpec;
    @Mock
    private CommandLine.IExitCodeExceptionMapper mockExitCodeMapper;
    @Captor
    private ArgumentCaptor<String> stringCaptor;

    private final StringWriter out = new StringWriter();
    private final StringWriter err = new StringWriter();
    private final ShortErrorMessageHandler handler = new ShortErrorMessageHandler();

    private CommandLine.Help.ColorScheme createColorScheme() {
        return CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.OFF);
    }

    @Test
    void shouldPrintFullUsageAndReturnOkWhenHelpFullArgPresent() {
        // Given
        when(mockCommandLine.getOut()).thenReturn(new PrintWriter(out));
        String[] args = {"--help-full"};
        CommandLine.ParameterException ex =
                new CommandLine.ParameterException(mockCommandLine, "test error");

        // When
        int exitCode = handler.handleParseException(ex, args);

        // Then
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.OK);
        verify(mockCommandLine).usage(any(PrintWriter.class));
    }

    @Test
    void shouldPrintErrorAndSuggestionsWhenHelpFullNotPresent() {
        // Given
        when(mockCommandLine.getErr()).thenReturn(new PrintWriter(err));
        when(mockCommandLine.getCommandSpec()).thenReturn(mockCommandSpec);
        when(mockCommandSpec.exitCodeOnInvalidInput()).thenReturn(CommandLine.ExitCode.USAGE);
        when(mockCommandLine.getColorScheme()).thenReturn(createColorScheme());
        String[] args = {};
        CommandLine.ParameterException ex =
                new CommandLine.ParameterException(mockCommandLine, "test error");

        // When
        int exitCode = handler.handleParseException(ex, args);

        // Then
        assertThat(exitCode).isEqualTo(CommandLine.ExitCode.USAGE);
        assertThat(err.toString()).contains("test error");
    }

    @Test
    void shouldUseMappedExitCodeWhenExitCodeMapperPresent() {
        // Given
        when(mockCommandLine.getExitCodeExceptionMapper()).thenReturn(mockExitCodeMapper);
        when(mockCommandLine.getErr()).thenReturn(new PrintWriter(err));
        when(mockExitCodeMapper.getExitCode(any())).thenReturn(42);
        when(mockCommandLine.getColorScheme()).thenReturn(createColorScheme());
        String[] args = {};
        CommandLine.ParameterException ex =
                new CommandLine.ParameterException(mockCommandLine, "test error");

        // When
        int exitCode = handler.handleParseException(ex, args);

        // Then
        assertThat(exitCode).isEqualTo(42);
        verify(mockExitCodeMapper).getExitCode(ex);
    }

    @Test
    void shouldUseDefaultExitCodeWhenNoExitCodeMapper() {
        // Given
        when(mockCommandLine.getExitCodeExceptionMapper()).thenReturn(null);
        when(mockCommandLine.getErr()).thenReturn(new PrintWriter(err));
        when(mockCommandLine.getCommandSpec()).thenReturn(mockCommandSpec);
        when(mockCommandSpec.exitCodeOnInvalidInput()).thenReturn(123);
        when(mockCommandLine.getColorScheme()).thenReturn(createColorScheme());
        String[] args = {};
        CommandLine.ParameterException ex =
                new CommandLine.ParameterException(mockCommandLine, "test error");

        // When
        int exitCode = handler.handleParseException(ex, args);

        // Then
        assertThat(exitCode).isEqualTo(123);
    }
}
