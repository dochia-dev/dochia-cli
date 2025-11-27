package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class LicensePlateGeneratorTest {

    private LicensePlateGenerator licensePlateGenerator;

    @BeforeEach
    void setup() {
        licensePlateGenerator = new LicensePlateGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "licenseplate,randomField,true",
            "plate,randomField,true",
            "not,licensePlate,true",
            "not,plateNumber,true",
            "not,vehiclePlate,true",
            "not,randomField,false"
    })
    void shouldRecognizeLicensePlate(String format, String property, boolean expected) {
        boolean isLicensePlate = licensePlateGenerator.appliesTo(format, property);
        Assertions.assertThat(isLicensePlate).isEqualTo(expected);
    }

    @Test
    void givenALicensePlateFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(licensePlateGenerator.getAlmostValidValue()).isEqualTo("ABCD-12345");
    }

    @Test
    void givenALicensePlateFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(licensePlateGenerator.getTotallyWrongValue()).isEqualTo("plate");
    }

    @Test
    void givenALicensePlateFormatGeneratorStrategy_whenGenerating_thenValidLicensePlateIsReturned() {
        String generated = (String) licensePlateGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated.length()).isGreaterThanOrEqualTo(6);
    }
}
