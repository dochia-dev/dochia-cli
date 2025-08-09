package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;

/**
 * A generator class implementing interfaces for generating valid key-value pairs data formats.
 * It implements the ValidDataFormatGenerator and OpenAPIFormat interfaces.
 */
@Singleton
public class KvPairsGenerator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "kvpairs".equalsIgnoreCase(format);
    }

    @Override
    public Object generate(Schema<?> schema) {
        return Map.of("key", "value", "anotherKey", "anotherValue");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("kvpairs");
    }
}
