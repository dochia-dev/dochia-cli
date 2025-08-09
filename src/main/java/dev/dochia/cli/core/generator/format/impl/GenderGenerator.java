package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * A generator class implementing interfaces for generating valid gender data formats.
 */
@Singleton
public class GenderGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    private static final List<String> GENDER = List.of("Male", "Female", "Other");
    private static final String GENDER_WORD = "gender";

    @Override
    public Object generate(Schema<?> schema) {
        return CommonUtils.selectRandom(GENDER);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return GENDER_WORD.equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).equalsIgnoreCase(GENDER_WORD);
    }

    @Override
    public List<String> matchingFormats() {
        return List.of(GENDER_WORD);
    }
}