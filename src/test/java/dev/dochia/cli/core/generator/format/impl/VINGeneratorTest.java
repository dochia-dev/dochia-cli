package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class VINGeneratorTest {

    private VINGenerator vinGenerator;

    @BeforeEach
    void setup() {
        vinGenerator = new VINGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "vin,randomField,true",
            "not,vin,true",
            "not,vehicleIdentificationNumber,true",
            "not,chassisNumber,true",
            "not,randomField,false"
    })
    void shouldRecognizeVIN(String format, String property, boolean expected) {
        boolean isVIN = vinGenerator.appliesTo(format, property);
        Assertions.assertThat(isVIN).isEqualTo(expected);
    }

    @Test
    void givenAVINFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(vinGenerator.getAlmostValidValue()).isEqualTo("1HGBH41JXMN1O9186");
    }

    @Test
    void givenAVINFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(vinGenerator.getTotallyWrongValue()).isEqualTo("VIN123");
    }

    @Test
    void givenAVINFormatGeneratorStrategy_whenGenerating_thenValidVINIsReturned() {
        String generated = (String) vinGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated).hasSize(17).matches("[A-HJ-NPR-Z0-9]{17}");
    }
}
