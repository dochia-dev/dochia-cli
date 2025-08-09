package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.List;

/**
 * A generator class implementing interfaces for generating valid Unix time data formats.
 * It implements the ValidDataFormatGenerator and OpenAPIFormat interfaces.
 */
@Singleton
public class UnixtimeGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "unixtime".equalsIgnoreCase(PropertySanitizer.sanitize(format));
    }

    @Override
    public Object generate(Schema<?> schema) {
        return Instant.now().minusSeconds(1).getEpochSecond();
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("unix-time", "unixtime", "unix_time");
    }
}
