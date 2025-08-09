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
 * Generates valid states.
 */
@Singleton
public class StateGenerator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "state".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("state") ||
                PropertySanitizer.sanitize(propertyName).endsWith("statename");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("state", "stateName", "state-name", "state_name");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.faker().address().state();

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}