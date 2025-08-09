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
 * Generates real line2 addresses.
 */
@Singleton
public class AddressLine2Generator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "line2".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("line2");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("line2");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String finalAddress = "Floor " + CommonUtils.random().nextInt(20) + ", Suite " + CommonUtils.random().nextInt(10);

        return DataFormat.matchesPatternOrNull(schema, finalAddress);
    }
}
