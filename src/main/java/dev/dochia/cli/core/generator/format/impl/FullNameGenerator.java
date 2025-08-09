package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.DataFormat;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Generator for full names.
 */
@Singleton
public class FullNameGenerator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "fullname".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("fullname");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("fullName", "full-name", "full_name");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.faker().name().fullName();

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}
