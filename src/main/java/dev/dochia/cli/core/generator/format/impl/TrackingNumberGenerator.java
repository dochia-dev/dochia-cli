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
 * A generator class implementing interfaces for generating valid and invalid tracking number data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class TrackingNumberGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        // Generate tracking number in various carrier formats
        int format = CommonUtils.random().nextInt(4);
        
        return switch (format) {
            case 0 -> {
                // UPS format: 1Z999AA10123456784
                yield "1Z" + generateAlphanumeric(6, true).toUpperCase(Locale.ROOT) + generateNumeric(10);
            }
            case 1 -> {
                // FedEx format: 123456789012
                yield generateNumeric(12);
            }
            case 2 -> {
                // USPS format: 9400 1000 0000 0000 0000 00
                yield "9400" + generateNumeric(4) + generateNumeric(4) + generateNumeric(4) + 
                        generateNumeric(4) + generateNumeric(2);
            }
            default -> {
                // DHL format: 1234567890
                yield generateNumeric(10);
            }
        };
    }

    private String generateNumeric(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(CommonUtils.random().nextInt(10));
        }
        return result.toString();
    }

    private String generateAlphanumeric(int length, boolean uppercase) {
        String chars = uppercase ? "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" : "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(CommonUtils.random().nextInt(chars.length())));
        }
        return result.toString();
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "tracking".equalsIgnoreCase(format) ||
                "trackingnumber".equalsIgnoreCase(format) ||
                sanitized.contains("tracking") ||
                (sanitized.contains("shipment") && (sanitized.contains("number") || sanitized.contains("id")));
    }

    @Override
    public String getAlmostValidValue() {
        // Invalid checksum
        return "1Z999AA1012345678X";
    }

    @Override
    public String getTotallyWrongValue() {
        return "TRACK-123";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("tracking", "tracking-number", "trackingnumber", "shipment-id");
    }
}
