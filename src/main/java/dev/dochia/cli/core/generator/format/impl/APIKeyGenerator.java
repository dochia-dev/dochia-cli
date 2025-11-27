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
 * A generator class implementing interfaces for generating valid and invalid API key data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class APIKeyGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Override
    public Object generate(Schema<?> schema) {
        // Generate API key in common formats
        int format = CommonUtils.random().nextInt(3);
        
        return switch (format) {
            case 0 -> generateSimpleKey(32); // Simple alphanumeric
            case 1 -> "sk_live_" + generateSimpleKey(24); // Stripe-style
            default -> "AIza" + generateSimpleKey(35); // Google-style
        };
    }

    private String generateSimpleKey(int length) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < length; i++) {
            key.append(CHARS.charAt(CommonUtils.random().nextInt(CHARS.length())));
        }
        return key.toString();
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "apikey".equalsIgnoreCase(format) ||
                sanitized.contains("apikey") ||
                (sanitized.contains("api") && sanitized.contains("key"));
    }

    @Override
    public String getAlmostValidValue() {
        // Too short to be valid
        return "sk_live_abc123";
    }

    @Override
    public String getTotallyWrongValue() {
        return "invalid-key";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("apikey", "api-key", "api_key");
    }
}
