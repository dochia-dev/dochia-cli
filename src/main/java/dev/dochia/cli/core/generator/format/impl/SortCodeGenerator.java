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
 * Generates real world sort codes.
 */
@Singleton
public class SortCodeGenerator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "sortcode".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("sortcode");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("sort-code", "sortCode");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.faker().numerify("## ## ##");

        return DataFormat.matchesPatternOrNullWithCombinations(schema, generated);
    }
}