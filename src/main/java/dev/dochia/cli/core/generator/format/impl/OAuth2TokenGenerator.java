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
 * A generator class implementing interfaces for generating valid and invalid OAuth2 bearer token data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class OAuth2TokenGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~+/";

    @Override
    public Object generate(Schema<?> schema) {
        // Generate OAuth2 bearer token (typically 40-200 characters)
        int length = 40 + CommonUtils.random().nextInt(160);
        StringBuilder token = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            token.append(CHARS.charAt(CommonUtils.random().nextInt(CHARS.length())));
        }
        
        return token.toString();
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "oauth2".equalsIgnoreCase(format) ||
                "bearer".equalsIgnoreCase(format) ||
                sanitized.contains("oauth") ||
                sanitized.contains("bearer") ||
                sanitized.contains("accesstoken") ||
                sanitized.contains("refreshtoken");
    }

    @Override
    public String getAlmostValidValue() {
        // Too short to be valid
        return "ya29.short";
    }

    @Override
    public String getTotallyWrongValue() {
        return "invalid token";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("oauth2", "bearer", "access-token", "refresh-token");
    }
}
