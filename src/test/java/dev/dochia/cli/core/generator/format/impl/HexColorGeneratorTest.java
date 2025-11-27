package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class HexColorGeneratorTest {

    private HexColorGenerator hexColorGenerator;

    @BeforeEach
    void setup() {
        hexColorGenerator = new HexColorGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "hexcolor,randomField,true",
            "color,randomField,true",
            "not,hexColor,true",
            "not,backgroundColor,true",
            "not,textColor,true",
            "not,multicolor,false",
            "not,randomField,false"
    })
    void shouldRecognizeHexColor(String format, String property, boolean expected) {
        boolean isHexColor = hexColorGenerator.appliesTo(format, property);
        Assertions.assertThat(isHexColor).isEqualTo(expected);
    }

    @Test
    void givenAHexColorFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(hexColorGenerator.getAlmostValidValue()).isEqualTo("#FF00F");
    }

    @Test
    void givenAHexColorFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(hexColorGenerator.getTotallyWrongValue()).isEqualTo("red");
    }

    @Test
    void givenAHexColorFormatGeneratorStrategy_whenGenerating_thenValidHexColorIsReturned() {
        String generated = (String) hexColorGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated).matches("^#[0-9A-F]{6}$");
    }
}
