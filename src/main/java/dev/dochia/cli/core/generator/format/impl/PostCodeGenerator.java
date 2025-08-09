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
 * Generates real world postal codes.
 */
@Singleton
public class PostCodeGenerator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "zip".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("postcode") ||
                PropertySanitizer.sanitize(propertyName).endsWith("postalcode") ||
                PropertySanitizer.sanitize(propertyName).endsWith("zip") ||
                PropertySanitizer.sanitize(propertyName).endsWith("zipcode") ||
                PropertySanitizer.sanitize(propertyName).endsWith("pincode");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("postCode", "post_code", "post-code", "postalCode", "postal_code", "postal-code",
                "zip", "zipCode", "zip_code", "zip-code", "pinCode", "pin-code", "pin_code");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.faker().address().zipCode();

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}