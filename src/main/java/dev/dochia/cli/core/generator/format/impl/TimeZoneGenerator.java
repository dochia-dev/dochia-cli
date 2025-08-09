package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.DataFormat;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/**
 * A generator class implementing various interfaces for generating valid data formats for time zones.
 */
@Singleton
public class TimeZoneGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    private static final Set<String> ALL_ZONES = ZoneId.getAvailableZoneIds();

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "timezone".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("timezone");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("timeZone", "time-zone", "time_zone");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.selectRandom(ALL_ZONES);

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}
