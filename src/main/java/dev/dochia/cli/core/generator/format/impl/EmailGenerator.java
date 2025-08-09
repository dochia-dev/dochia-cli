package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Locale;

/**
 * A generator class implementing various interfaces for generating valid and invalid email data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class EmailGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    private static final String EMAIL = "email";

    @Override
    public Object generate(Schema<?> schema) {
        String hero = CommonUtils.faker().ancient().hero().toLowerCase(Locale.ROOT);
        String color = CommonUtils.faker().color().name().toLowerCase(Locale.ROOT);

        return "%s.%s@dochia.dev".formatted(hero, color).replace(" ", "-");
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return propertyName.toLowerCase(Locale.ROOT).endsWith(EMAIL) ||
                PropertySanitizer.sanitize(propertyName).endsWith("emailaddress") ||
                EMAIL.equalsIgnoreCase(format);
    }

    @Override
    public String getAlmostValidValue() {
        return "email@bubu.";
    }

    @Override
    public String getTotallyWrongValue() {
        return "bubulina";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of(EMAIL);
    }
}
