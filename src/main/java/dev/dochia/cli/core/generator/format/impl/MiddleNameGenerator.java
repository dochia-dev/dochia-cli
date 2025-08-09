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
 * Generator for middle names.
 */
@Singleton
public class MiddleNameGenerator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "middlename".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("middlename");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("middleNAme", "middlename", "middle-name", "middle_name");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.faker().name().firstName();

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}
