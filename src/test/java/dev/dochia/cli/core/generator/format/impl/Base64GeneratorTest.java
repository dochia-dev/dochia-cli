package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class Base64GeneratorTest {

    private Base64Generator base64Generator;

    @BeforeEach
    void setup() {
        base64Generator = new Base64Generator();
    }

    @ParameterizedTest
    @CsvSource({
            "base64,randomField,true",
            "byte,randomField,true",
            "not,base64Encoded,true",
            "not,dataEncoded,true",
            "not,randomField,false"
    })
    void shouldRecognizeBase64(String format, String property, boolean expected) {
        boolean isBase64 = base64Generator.appliesTo(format, property);
        Assertions.assertThat(isBase64).isEqualTo(expected);
    }

    @Test
    void givenABase64FormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(base64Generator.getAlmostValidValue()).isEqualTo("SGVsbG8gV29ybGQ@");
    }

    @Test
    void givenABase64FormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(base64Generator.getTotallyWrongValue()).isEqualTo("not base64!@#$%");
    }

    @Test
    void givenABase64FormatGeneratorStrategy_whenGenerating_thenValidBase64IsReturned() {
        String generated = (String) base64Generator.generate(null);
        Assertions.assertThat(generated).isNotNull().matches("^[A-Za-z0-9+/]+=*$");
    }
}
