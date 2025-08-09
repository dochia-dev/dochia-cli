package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.generator.simple.StringGenerator;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * A generator class implementing interfaces for generating valid GTIN-8 (Global Trade Item Number) data formats.
 * It implements the ValidDataFormatGenerator and OpenAPIFormat interfaces.
 */
@Singleton
public class Gtin8Generator implements ValidDataFormatGenerator, OpenAPIFormat {
    @Override
    public Object generate(Schema<?> schema) {
        return StringGenerator.generate("[0-9]+", 8, 8);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "ean8".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                "gtin8".equalsIgnoreCase(PropertySanitizer.sanitize(format));
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("gtin8", "ean8");
    }
}
