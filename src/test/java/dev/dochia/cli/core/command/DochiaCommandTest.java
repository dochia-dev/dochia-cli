package dev.dochia.cli.core.command;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class DochiaCommandTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private DochiaCommand dochiaCommand;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        dochiaCommand = new DochiaCommand();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void shouldDisplayHelpWhenNoArguments() {
        // When
        dochiaCommand.run();

        // Then
        assertThat(outContent.toString())
                .contains("dochia – Bringing chaos with love!")
                .contains("dochia automatically generates and executes")
                .contains("Examples:");
    }

    @Test
    void shouldDisplayHelpWhenHelpOption() {
        // Given
        dochiaCommand.licenses = false;

        // When
        dochiaCommand.run();

        // Then
        assertThat(outContent.toString())
                .contains("dochia – Bringing chaos with love!")
                .contains("dochia automatically generates");
    }

    @Test
    void shouldDisplayLicenseWhenLicensesOption() {
        // Given
        dochiaCommand.licenses = true;

        // When
        dochiaCommand.run();

        // Then - Just verify it doesn't throw an exception
        assertThat(outContent.toString()).isNotEmpty();
    }

    @Test
    void shouldReturnZeroAsExitCode() {
        // When
        int exitCode = dochiaCommand.getExitCode();

        // Then
        assertThat(exitCode).isZero();
    }

    @Test
    void shouldDisplayLicensesSuccessfully() {
        // When
        int result = dochiaCommand.displayLicenses();

        // Then
        assertThat(result).isZero();
        assertThat(outContent.toString()).isNotEmpty();
    }
}
