package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;

/**
 * A generator class implementing interfaces for generating valid Internationalized Resource Identifiers (IRI) data formats.
 * It implements the ValidDataFormatGenerator and OpenAPIFormat interfaces.
 */
@Singleton
public class IRIReferenceGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    @Override
    public Object generate(Schema<?> schema) {
        String generated = RandomStringUtils.secure().nextAlphabetic(5);
        return "/füzzing%s/".formatted(generated);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "iri-reference".equalsIgnoreCase(format);
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("iri-reference");
    }
}
