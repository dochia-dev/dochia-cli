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
 * Generates meaningful titles like book titles, article titles, etc.
 */
@Singleton
public class TitleGenerator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitizedProperty = PropertySanitizer.sanitize(propertyName);
        return "title".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                (sanitizedProperty.endsWith("title") && !sanitizedProperty.endsWith("jobtitle"));
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("title");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.faker().book().title();

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}
