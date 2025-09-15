package dev.dochia.cli.core.command;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class LegendCommandTest {

    private PrintStream originalOut;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private String run(String... args) {
        LegendCommand cmd = new LegendCommand();
        int exit = new CommandLine(cmd).execute(args);
        assertThat(exit).isZero();
        return outContent.toString(StandardCharsets.UTF_8);
    }

    @Test
    void haiku_en_outputsExpectedThreeLines_andNoAnsiWhenNoColor() {
        String out = run("--haiku", "--no-color");

        // Assert key haiku lines are present
        assertThat(out)
                .contains("Nine coats on the hill,")
                .contains("Spring fooled her, winter returned —")
                .contains("Stone remembers all.");

        // Ensure no ANSI escape sequences when --no-color is set
        assertThat(out).doesNotContain("\u001B[");
    }

    @Test
    void ascii_en_containsAsciiArt_andPunchline_andNoAnsiWhenNoColor() {
        String out = run("--ascii", "--no-color");

        assertThat(out)
                .contains("   /\\    Baba Dochia climbed high,")
                .contains("  /  \\   dropping her nine coats…")
                .contains(" / ❄  \\  …then winter laughed.")
                .contains("/______\\ Moral: never trust inputs. 😉");

        assertThat(out).doesNotContain("\u001B[");
    }

    @Test
    void defaultVariant_en_isOneOfKnownOutputs_andNoAnsiWhenNoColor() {
        String out = run("--no-color", "--seed", "42");

        assertThat(out).satisfiesAnyOf(
                s -> assertThat(s).contains("Dochia’s Legend")
                        .contains("Baba Dochia climbed the mountain,")
                        .contains("Never trust the weather forecast,"),
                s -> assertThat(s).contains("Nine coats on the hill,")
                        .contains("Stone remembers all."),
                s -> assertThat(s).contains("Baba Dochia climbed high,")
                        .contains("Moral: never trust inputs.")
        );

        assertThat(out).doesNotContain("\u001B[");
    }
}

