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
 * Generates real world person names.
 */
@Singleton
public class CardholderNameGenerator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "personname".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("cardholdername");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("cardHolderName", "card_holder_name", "card-holder-name", "");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.faker().name().name();

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}