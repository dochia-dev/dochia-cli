package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class TrackingNumberGeneratorTest {

    private TrackingNumberGenerator trackingNumberGenerator;

    @BeforeEach
    void setup() {
        trackingNumberGenerator = new TrackingNumberGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "tracking,randomField,true",
            "trackingnumber,randomField,true",
            "not,trackingNumber,true",
            "not,shipmentNumber,true",
            "not,shipmentId,true",
            "not,randomField,false"
    })
    void shouldRecognizeTrackingNumber(String format, String property, boolean expected) {
        boolean isTrackingNumber = trackingNumberGenerator.appliesTo(format, property);
        Assertions.assertThat(isTrackingNumber).isEqualTo(expected);
    }

    @Test
    void givenATrackingNumberFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(trackingNumberGenerator.getAlmostValidValue()).isEqualTo("1Z999AA1012345678X");
    }

    @Test
    void givenATrackingNumberFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(trackingNumberGenerator.getTotallyWrongValue()).isEqualTo("TRACK-123");
    }

    @Test
    void givenATrackingNumberFormatGeneratorStrategy_whenGenerating_thenValidTrackingNumberIsReturned() {
        String generated = (String) trackingNumberGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated.length()).isGreaterThanOrEqualTo(10);
    }
}
