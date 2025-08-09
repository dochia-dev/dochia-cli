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
 * Generates valid flight codes
 */
@Singleton
public class FlightCodeGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    private static final List<String> AIRLINE_CODES = Arrays.asList(
            "AA", // American Airlines
            "DL", // Delta
            "UA", // United Airlines
            "BA", // British Airways
            "LH",  // Lufthansa
            "RO", // Tarom
            "AF" // Air France
    );

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "flightcode".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("flightcode");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("flightCode", "flight-code", "flight_code");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String airlineCode = AIRLINE_CODES.get(CommonUtils.random().nextInt(AIRLINE_CODES.size()));
        int flightNumber = 100 + CommonUtils.random().nextInt(900);
        String generated = airlineCode + flightNumber;

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}
