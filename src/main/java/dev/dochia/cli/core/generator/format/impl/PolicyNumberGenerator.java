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
 * A generator class implementing interfaces for generating valid and invalid insurance policy number data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class PolicyNumberGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        // Generate insurance policy number in various formats
        int format = CommonUtils.random().nextInt(3);

        return switch (format) {
            case 0 -> // Format: POL-12345678
                    "POL-" + String.format("%08d", CommonUtils.random().nextInt(100000000));
            case 1 -> {
                // Format: AB-1234-5678-90
                String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                yield "" + letters.charAt(CommonUtils.random().nextInt(letters.length())) +
                        letters.charAt(CommonUtils.random().nextInt(letters.length())) + "-" +
                        String.format("%04d-%04d-%02d",
                                CommonUtils.random().nextInt(10000),
                                CommonUtils.random().nextInt(10000),
                                CommonUtils.random().nextInt(100));
            }
            default -> // Format: 123456789012 (12 digits)
                    String.format("%012d", CommonUtils.random().nextLong(1000000000000L));
        };
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "policy".equalsIgnoreCase(format) ||
                "policynumber".equalsIgnoreCase(format) ||
                (sanitized.contains("policy") && (sanitized.contains("number") || sanitized.contains("id"))) ||
                sanitized.contains("insurancenumber");
    }

    @Override
    public String getAlmostValidValue() {
        // Invalid format
        return "POL-ABC";
    }

    @Override
    public String getTotallyWrongValue() {
        return "policy";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("policy", "policy-number", "policynumber", "insurance-number");
    }
}
