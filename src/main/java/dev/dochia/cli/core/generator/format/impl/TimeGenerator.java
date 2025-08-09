package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A generator class implementing interfaces for generating valid time data formats.
 * It implements the ValidDataFormatGenerator and OpenAPIFormat interfaces.
 */
@Singleton
public class TimeGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    @Override
    public Object generate(Schema<?> schema) {
        return OffsetTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_OFFSET_TIME);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "time".equalsIgnoreCase(format);
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("time");
    }
}
