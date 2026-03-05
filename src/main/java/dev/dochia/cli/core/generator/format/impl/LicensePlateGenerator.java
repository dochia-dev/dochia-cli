package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * A generator class implementing interfaces for generating valid and invalid license plate data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class LicensePlateGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        // Generate license plate in various formats
        int format = CommonUtils.random().nextInt(4);

        return switch (format) {
            case 0 -> // US format: ABC-1234
                    generateLetters(3) + "-" + generateNumbers(4);
            case 1 -> // US format: 1ABC234
                    generateNumbers(1) + generateLetters(3) + generateNumbers(3);
            case 2 -> // EU format: AB-123-CD
                    generateLetters(2) + "-" + generateNumbers(3) + "-" + generateLetters(2);
            default -> // Simple format: ABC1234
                    generateLetters(3) + generateNumbers(4);
        };
    }

    private String generateLetters(int count) {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(letters.charAt(CommonUtils.random().nextInt(letters.length())));
        }
        return result.toString();
    }

    private String generateNumbers(int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(CommonUtils.random().nextInt(10));
        }
        return result.toString();
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "licenseplate".equalsIgnoreCase(format) ||
                "plate".equalsIgnoreCase(format) ||
                sanitized.contains("licenseplate") ||
                sanitized.contains("licensenumber") ||
                sanitized.contains("platenumber") ||
                sanitized.contains("vehicleplate");
    }

    @Override
    public String getAlmostValidValue() {
        // Too many characters
        return "ABCD-12345";
    }

    @Override
    public String getTotallyWrongValue() {
        return "plate";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("licenseplate", "license-plate", "plate", "vehicle-plate");
    }
}
