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
 * Generates real addresses.
 */
@Singleton
public class AddressGenerator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return ("address".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("address"))
                && !PropertySanitizer.sanitize(propertyName).contains("email");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("address");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.faker().address().fullAddress();

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}