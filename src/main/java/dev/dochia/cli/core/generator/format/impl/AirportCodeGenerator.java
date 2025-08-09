package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.DataFormat;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.List;

/**
 * Generates valid airport codes.
 */
@Singleton
public class AirportCodeGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    private static final List<String> AIRPORT_CODES = Arrays.asList(
            "LHR", // London Heathrow
            "JFK", // New York John F. Kennedy
            "SFO", // San Francisco International
            "LAX", // Los Angeles International
            "FRA", // Frankfurt Airport
            "AMS", // Amsterdam Schiphol
            "HND", // Tokyo Haneda
            "SYD", // Sydney Kingsford Smith
            "DXB", // Dubai International
            "PEK", // Beijing Capital
            "GRU", // São Paulo–Guarulhos
            "CDG", // Paris Charles de Gaulle
            "NRT", // Tokyo Narita
            "YYZ", // Toronto Pearson
            "MEX", // Mexico City International
            "ORD"  // Chicago O'Hare
    );

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "airportcode".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                "airport".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("airportcode") ||
                PropertySanitizer.sanitize(propertyName).endsWith("airport");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("airportCode", "airport-code", "airport_code", "airport");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.selectRandom(AIRPORT_CODES);

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}