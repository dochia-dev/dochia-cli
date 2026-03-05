package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class LatLongGeneratorTest {

    private LatLongGenerator latLongGenerator;

    @BeforeEach
    void setup() {
        latLongGenerator = new LatLongGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "latlong,randomField,true",
            "latlng,randomField,true",
            "coordinate,randomField,true",
            "not,latitude,true",
            "not,longitude,true",
            "not,lat,true",
            "not,lng,true",
            "not,lon,true",
            "not,randomField,false"
    })
    void shouldRecognizeLatLong(String format, String property, boolean expected) {
        boolean isLatLong = latLongGenerator.appliesTo(format, property);
        Assertions.assertThat(isLatLong).isEqualTo(expected);
    }

    @Test
    void givenALatLongFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(latLongGenerator.getAlmostValidValue()).isEqualTo("95.123456");
    }

    @Test
    void givenALatLongFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(latLongGenerator.getTotallyWrongValue()).isEqualTo("invalid-coordinate");
    }

    @Test
    void givenALatLongFormatGeneratorStrategy_whenGenerating_thenValidCoordinateIsReturned() {
        String generated = (String) latLongGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull().containsPattern("-?\\d+\\.\\d+");
    }
}
