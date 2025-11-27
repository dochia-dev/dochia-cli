package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * A generator class implementing interfaces for generating valid and invalid cron expression data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class CronExpressionGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    private static final List<String> COMMON_CRON_EXPRESSIONS = List.of(
            "0 0 * * *",        // Daily at midnight
            "0 */6 * * *",      // Every 6 hours
            "*/15 * * * *",     // Every 15 minutes
            "0 9 * * 1",        // Every Monday at 9 AM
            "0 0 1 * *",        // First day of month at midnight
            "0 12 * * 1-5",     // Weekdays at noon
            "0 0 * * 0",        // Every Sunday at midnight
            "30 2 * * *",       // Daily at 2:30 AM
            "0 */4 * * *",      // Every 4 hours
            "0 0 1 1 *"         // January 1st at midnight
    );

    @Override
    public Object generate(Schema<?> schema) {
        return CommonUtils.selectRandom(COMMON_CRON_EXPRESSIONS);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "cron".equalsIgnoreCase(format) ||
                sanitized.contains("cron") ||
                (sanitized.contains("schedule") && sanitized.contains("expression"));
    }

    @Override
    public String getAlmostValidValue() {
        // Invalid minute value (60)
        return "60 0 * * *";
    }

    @Override
    public String getTotallyWrongValue() {
        return "not a cron";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("cron", "cron-expression");
    }
}
