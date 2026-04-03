package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.util.CommonUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

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
            "TRACKING,randomField,true",
            "trackingnumber,randomField,true",
            "TRACKINGNUMBER,randomField,true",
            "not,trackingNumber,true",
            "not,tracking_number,true",
            "not,shipmentNumber,true",
            "not,shipmentId,true",
            "not,shipment-id,true",
            "not,randomField,false",
            "other,shipment,false",
            "other,other,false"
    })
    void shouldRecognizeTrackingNumber(String format, String property, boolean expected) {
        assertThat(trackingNumberGenerator.appliesTo(format, property)).isEqualTo(expected);
    }

    @Test
    void givenATrackingNumberFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        assertThat(trackingNumberGenerator.getAlmostValidValue()).isEqualTo("1Z999AA1012345678X");
    }

    @Test
    void givenATrackingNumberFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        assertThat(trackingNumberGenerator.getTotallyWrongValue()).isEqualTo("TRACK-123");
    }

    @Test
    void shouldReturnMatchingFormats() {
        assertThat(trackingNumberGenerator.matchingFormats())
                .containsExactly("tracking", "tracking-number", "trackingnumber", "shipment-id");
    }

    @Test
    void shouldGenerateUpsFormat() {
        // seed 4096 → nextInt(4)==0
        CommonUtils.initRandom(4096);
        String generated = (String) trackingNumberGenerator.generate(null);

        assertThat(generated).isNotNull()
                .startsWith("1Z")
                .hasSizeGreaterThanOrEqualTo(18);
    }

    @Test
    void shouldGenerateFedExFormat() {
        // seed 6144 → nextInt(4)==1
        CommonUtils.initRandom(6144);
        String generated = (String) trackingNumberGenerator.generate(null);

        assertThat(generated).isNotNull()
                .hasSize(12)
                .matches("\\d{12}");
    }

    @Test
    void shouldGenerateUspsFormat() {
        // seed 1 → nextInt(4)==2
        CommonUtils.initRandom(1);
        String generated = (String) trackingNumberGenerator.generate(null);

        assertThat(generated).isNotNull()
                .startsWith("9400")
                .hasSizeGreaterThanOrEqualTo(20);
    }

    @Test
    void shouldGenerateDhlFormat() {
        // seed 256 → nextInt(4)==3
        CommonUtils.initRandom(256);
        String generated = (String) trackingNumberGenerator.generate(null);

        assertThat(generated).isNotNull()
                .hasSize(10)
                .matches("\\d{10}");
    }

    @Test
    void shouldGenerateNonNullAcrossMultipleSeeds() {
        for (int seed = 0; seed < 20; seed++) {
            CommonUtils.initRandom(seed);
            String generated = (String) trackingNumberGenerator.generate(null);
            assertThat(generated).as("seed=%d", seed)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSizeGreaterThanOrEqualTo(10);
        }
    }
}
