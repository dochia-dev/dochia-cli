package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.DochiaRandom;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * A generator class implementing interfaces for generating valid URI (Uniform Resource Identifier) reference data formats.
 * It implements the ValidDataFormatGenerator and OpenAPIFormat interfaces.
 */
@Singleton
public class URIReferenceGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    @Override
    public Object generate(Schema<?> schema) {
        return "/fuzzing%s/".formatted(DochiaRandom.alphabetic(4));
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "uri-reference".equalsIgnoreCase(format);
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("uri-reference");
    }
}
