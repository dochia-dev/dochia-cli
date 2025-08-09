package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;

/**
 * A generator class implementing interfaces for generating valid ISBN-10 (International Standard Book Number) data formats.
 * It implements the ValidDataFormatGenerator and OpenAPIFormat interfaces.
 */
@Singleton
public class ISBN10Generator implements ValidDataFormatGenerator, OpenAPIFormat {

    private static final String ISBN_10 = "isbn10";
    private static final String ISBN = "isbn";

    @Override
    public Object generate(Schema<?> schema) {
        return RandomStringUtils.secure().nextNumeric(10, 10);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return ISBN_10.equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                ISBN.equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith(ISBN_10) ||
                PropertySanitizer.sanitize(propertyName).endsWith(ISBN);
    }

    @Override
    public List<String> matchingFormats() {
        return List.of(ISBN_10, ISBN);
    }
}
