package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

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
            "LATLONG,randomField,true",
            "latlng,randomField,true",
            "LATLNG,randomField,true",
            "coordinate,randomField,true",
            "COORDINATE,randomField,true",
            "coordinates,randomField,true",
            "not,latitude,true",
            "not,longitude,true",
            "not,latlong,true",
            "not,latlng,true",
            "not,lat,true",
            "not,lng,true",
            "not,lon,true",
            "not,randomField,false",
            "other,other,false"
    })
    void shouldRecognizeLatLong(String format, String property, boolean expected) {
        assertThat(latLongGenerator.appliesTo(format, property)).isEqualTo(expected);
    }

    @Test
    void givenALatLongFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        assertThat(latLongGenerator.getAlmostValidValue()).isEqualTo("95.123456");
    }

    @Test
    void givenALatLongFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        assertThat(latLongGenerator.getTotallyWrongValue()).isEqualTo("invalid-coordinate");
    }

    @Test
    void shouldReturnMatchingFormats() {
        assertThat(latLongGenerator.matchingFormats())
                .containsExactly("latlong", "latlng", "coordinate", "coordinates", "latitude", "longitude");
    }

    @Test
    void shouldGenerateLatitudeWhenSchemaNameContainsLat() {
        Schema<String> schema = new Schema<>();
        schema.setName("latitude");

        String generated = (String) latLongGenerator.generate(schema);

        assertThat(generated).isNotNull()
                .matches("-?\\d+\\.\\d{6}")
                .satisfies(s -> {
                    double val = Double.parseDouble(s);
                    assertThat(val).isBetween(-90.0, 90.0);
                });
    }

    @Test
    void shouldGenerateLongitudeWhenSchemaNameContainsLon() {
        Schema<String> schema = new Schema<>();
        schema.setName("longitude");

        String generated = (String) latLongGenerator.generate(schema);

        assertThat(generated).isNotNull()
                .matches("-?\\d+\\.\\d{6}")
                .satisfies(s -> {
                    double val = Double.parseDouble(s);
                    assertThat(val).isBetween(-180.0, 180.0);
                });
    }

    @Test
    void shouldGenerateLongitudeWhenSchemaNameContainsLng() {
        Schema<String> schema = new Schema<>();
        schema.setName("lng");

        String generated = (String) latLongGenerator.generate(schema);

        assertThat(generated).isNotNull()
                .matches("-?\\d+\\.\\d{6}")
                .satisfies(s -> {
                    double val = Double.parseDouble(s);
                    assertThat(val).isBetween(-180.0, 180.0);
                });
    }

    @Test
    void shouldGenerateCoordinatePairWhenSchemaIsNull() {
        String generated = (String) latLongGenerator.generate(null);

        assertThat(generated).isNotNull()
                .contains(",")
                .matches("-?\\d+\\.\\d{6},-?\\d+\\.\\d{6}");
    }

    @Test
    void shouldGenerateCoordinatePairWhenSchemaNameIsNull() {
        Schema<String> schema = new Schema<>();

        String generated = (String) latLongGenerator.generate(schema);

        assertThat(generated).isNotNull()
                .contains(",")
                .matches("-?\\d+\\.\\d{6},-?\\d+\\.\\d{6}");
    }

    @Test
    void shouldGenerateCoordinatePairWhenSchemaNameDoesNotMatchLatOrLon() {
        Schema<String> schema = new Schema<>();
        schema.setName("position");

        String generated = (String) latLongGenerator.generate(schema);

        assertThat(generated).isNotNull()
                .contains(",")
                .matches("-?\\d+\\.\\d{6},-?\\d+\\.\\d{6}");
    }
}
