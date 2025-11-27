package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.Base64;
import java.util.List;

/**
 * A generator class implementing interfaces for generating valid and invalid Base64 encoded data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class Base64Generator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        // Generate random bytes and encode to Base64
        int length = 16 + CommonUtils.random().nextInt(32); // 16-48 bytes
        byte[] randomBytes = new byte[length];
        CommonUtils.random().nextBytes(randomBytes);
        
        return Base64.getEncoder().encodeToString(randomBytes);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "base64".equalsIgnoreCase(format) ||
                "byte".equalsIgnoreCase(format) || // OpenAPI uses 'byte' for base64
                sanitized.contains("base64") ||
                sanitized.endsWith("encoded");
    }

    @Override
    public String getAlmostValidValue() {
        // Invalid Base64 character '@'
        return "SGVsbG8gV29ybGQ@";
    }

    @Override
    public String getTotallyWrongValue() {
        return "not base64!@#$%";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("base64", "byte");
    }
}
