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
 * Generates valid last names.
 */
@Singleton
public class LastNameGenerator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "lastname".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("lastname");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("lastName", "lastname", "last-name", "last_name");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.faker().name().lastName();

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}