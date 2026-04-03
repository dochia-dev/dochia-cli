package dev.dochia.cli.core.command;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
                .contains("Stone remembers all.")
                .doesNotContain("\u001B[");
    }

    @Test
    void ascii_en_containsAsciiArt_andPunchline_andNoAnsiWhenNoColor() {
        String out = run("--ascii", "--no-color");

        assertThat(out)
                .contains("   /\\    Baba Dochia climbed high,")
                .contains("  /  \\   dropping her nine coats…")
                .contains(" / ❄  \\  …then winter laughed.")
                .contains("/______\\ Moral: never trust inputs. 😉")
                .doesNotContain("\u001B[");
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
        ).doesNotContain("\u001B[");
    }

    @Test
    void shouldPreferHaikuWhenBothHaikuAndAsciiSpecified() {
        String out = run("--haiku", "--ascii", "--no-color");

        assertThat(out)
                .contains("Nine coats on the hill,")
                .contains("Stone remembers all.")
                .doesNotContain("Baba Dochia climbed high,");
    }

    @Test
    void shouldDisplayStoryVariantWithSeedZero() {
        String out = run("--no-color", "--seed", "0");

        assertThat(out)
                .contains("Dochia’s Legend")
                .contains("Baba Dochia climbed the mountain,")
                .contains("wearing nine coats")
                .contains("Never trust the weather forecast,");
    }

    @Test
    void shouldOutputContentWhenColorEnabled() {
        String out = run("--seed", "0");

        assertThat(out).isNotEmpty()
                .contains("Dochia’s Legend");
    }

    @Test
    void shouldRespectWidthOption() {
        String out = run("--no-color", "--seed", "0", "--width", "80");

        assertThat(out).isNotEmpty()
                .contains("Dochia’s Legend");
    }

    @ParameterizedTest
    @CsvSource({"20", "60", "120"})
    void shouldHandleVariousWidths(String width) {
        String out = run("--no-color", "--seed", "0", "--width", width);

        assertThat(out).isNotEmpty();
    }

    @Test
    void shouldContainLineConstant() {
        assertThat(LegendCommand.LINE)
                .isNotEmpty()
                .contains("\u2500");
    }

    @Test
    void shouldHandleAsciiWithAnsiEnabled() {
        String out = run("--ascii");

        assertThat(out).isNotEmpty()
                .contains("Baba Dochia climbed high,");
    }

    @Test
    void shouldHandleHaikuWithAnsiEnabled() {
        String out = run("--haiku");

        assertThat(out).isNotEmpty()
                .contains("Nine coats on the hill,");
    }

    @Test
    void shouldHandleRandomVariantWithoutSeed() {
        String out = run("--no-color");

        assertThat(out).isNotEmpty()
                .satisfiesAnyOf(
                        s -> assertThat(s).contains("Dochia’s Legend"),
                        s -> assertThat(s).contains("Nine coats on the hill,"),
                        s -> assertThat(s).contains("Baba Dochia climbed high,")
                );
    }
}

