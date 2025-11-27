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
 * A generator class implementing interfaces for generating valid and invalid Tax ID/EIN data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class TaxIdGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        // Generate US EIN format: XX-XXXXXXX
        int part1 = 10 + CommonUtils.random().nextInt(90);
        int part2 = 1000000 + CommonUtils.random().nextInt(9000000);
        
        return String.format("%02d-%07d", part1, part2);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "taxid".equalsIgnoreCase(format) ||
                "ein".equalsIgnoreCase(format) ||
                sanitized.contains("taxid") ||
                sanitized.contains("ein") ||
                sanitized.contains("taxpayerid") ||
                sanitized.contains("federaltaxid") ||
                sanitized.contains("employeridentificationnumber");
    }

    @Override
    public String getAlmostValidValue() {
        // Invalid format (missing digit)
        return "12-345678";
    }

    @Override
    public String getTotallyWrongValue() {
        return "ABC-123";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("taxid", "tax-id", "ein", "federal-tax-id");
    }
}
