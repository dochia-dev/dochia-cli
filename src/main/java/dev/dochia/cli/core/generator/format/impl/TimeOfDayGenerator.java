package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Generates random time of day in HH:mm format.
 */
@Singleton
public class TimeOfDayGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    @Override
    public Object generate(Schema<?> schema) {
        int hour = CommonUtils.random().nextInt(24);
        int minute = CommonUtils.random().nextInt(60);

        return String.format("%02d:%02d", hour, minute);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return PropertySanitizer.sanitize(propertyName).endsWith("time") &&
                !"time".equalsIgnoreCase(format);
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("time");
    }
}
