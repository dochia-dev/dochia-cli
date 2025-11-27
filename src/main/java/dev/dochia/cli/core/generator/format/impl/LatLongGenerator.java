package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Locale;

/**
 * A generator class implementing interfaces for generating valid and invalid latitude/longitude coordinate data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class LatLongGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        String propertyName = (schema != null && schema.getName() != null) ? schema.getName().toLowerCase(Locale.ROOT) : "";
        
        // Generate latitude or longitude based on property name
        if (propertyName.contains("lat")) {
            // Latitude: -90 to 90
            double lat = -90 + (CommonUtils.random().nextDouble() * 180);
            return String.format(Locale.US, "%.6f", lat);
        } else if (propertyName.contains("lon") || propertyName.contains("lng")) {
            // Longitude: -180 to 180
            double lon = -180 + (CommonUtils.random().nextDouble() * 360);
            return String.format(Locale.US, "%.6f", lon);
        }
        
        // Default: return a coordinate pair
        double lat = -90 + (CommonUtils.random().nextDouble() * 180);
        double lon = -180 + (CommonUtils.random().nextDouble() * 360);
        return String.format(Locale.US, "%.6f,%.6f", lat, lon);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "latlong".equalsIgnoreCase(format) ||
                "latlng".equalsIgnoreCase(format) ||
                "coordinate".equalsIgnoreCase(format) ||
                "coordinates".equalsIgnoreCase(format) ||
                sanitized.contains("latitude") ||
                sanitized.contains("longitude") ||
                sanitized.contains("latlong") ||
                sanitized.contains("latlng") ||
                sanitized.endsWith("lat") ||
                sanitized.endsWith("lon") ||
                sanitized.endsWith("lng");
    }

    @Override
    public String getAlmostValidValue() {
        // Latitude out of range
        return "95.123456";
    }

    @Override
    public String getTotallyWrongValue() {
        return "invalid-coordinate";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("latlong", "latlng", "coordinate", "coordinates", "latitude", "longitude");
    }
}
