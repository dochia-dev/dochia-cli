package dev.dochia.cli.core.command;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

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
        dochiaCommand.run();

        assertThat(outContent.toString(StandardCharsets.UTF_8))
                .contains("dochia – Bringing chaos with love!")
                .contains("dochia automatically generates and executes")
                .contains("Examples:");
    }

    @Test
    void shouldDisplayHelpWhenHelpOption() {
        dochiaCommand.licenses = false;
        dochiaCommand.run();

        assertThat(outContent.toString(StandardCharsets.UTF_8))
                .contains("dochia – Bringing chaos with love!")
                .contains("dochia automatically generates");
    }

    @Test
    void shouldDisplayLicenseWhenLicensesOption() {
        dochiaCommand.licenses = true;
        dochiaCommand.run();

        assertThat(outContent.toString(StandardCharsets.UTF_8)).isNotEmpty();
    }

    @Test
    void shouldReturnZeroExitCodeByDefault() {
        assertThat(dochiaCommand.getExitCode()).isZero();
    }

    @Test
    void shouldReturnZeroExitCodeAfterDisplayingHelp() {
        dochiaCommand.run();

        assertThat(dochiaCommand.getExitCode()).isZero();
    }

    @Test
    void shouldSetExitCodeAfterLicensesRun() {
        dochiaCommand.licenses = true;
        dochiaCommand.run();

        assertThat(dochiaCommand.getExitCode()).isZero();
        assertThat(outContent.toString(StandardCharsets.UTF_8)).isNotEmpty();
    }

    @Test
    void shouldDisplayLicensesSuccessfully() {
        int result = dochiaCommand.displayLicenses();

        assertThat(result).isZero();
        assertThat(outContent.toString(StandardCharsets.UTF_8)).isNotEmpty();
    }

    @Test
    void shouldReturnNonZeroWhenLicenseFileNotFound() {
        // Use a custom classloader that won't find the resource
        Thread currentThread = Thread.currentThread();
        ClassLoader original = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(new ClassLoader(null) {});
            int result = dochiaCommand.displayLicenses();

            assertThat(result).isEqualTo(1);
            assertThat(errContent.toString(StandardCharsets.UTF_8))
                    .contains("License file not found.");
        } finally {
            currentThread.setContextClassLoader(original);
        }
    }

    @Test
    void shouldNotPrintHelpWhenLicensesFlagIsSet() {
        dochiaCommand.licenses = true;
        dochiaCommand.run();

        assertThat(outContent.toString(StandardCharsets.UTF_8))
                .doesNotContain("dochia – Bringing chaos with love!");
    }
}
