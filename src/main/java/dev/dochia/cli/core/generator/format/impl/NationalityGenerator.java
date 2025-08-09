package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Generates real world nationalities.
 */
@Singleton
public class NationalityGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    private final CountryCodeGenerator countryCodeGenerator;

    public NationalityGenerator(CountryCodeGenerator countryCodeGenerator) {
        this.countryCodeGenerator = countryCodeGenerator;
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "nationality".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("nationality");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("nationality");
    }

    @Override
    public Object generate(Schema<?> schema) {
        return countryCodeGenerator.generate(schema);
    }
}