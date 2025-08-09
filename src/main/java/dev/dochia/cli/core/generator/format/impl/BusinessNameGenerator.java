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
 * Generates real world company names.
 */
@Singleton
public class BusinessNameGenerator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "company".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("company") ||
                PropertySanitizer.sanitize(propertyName).endsWith("companyname") ||
                PropertySanitizer.sanitize(propertyName).endsWith("businessname") ||
                PropertySanitizer.sanitize(propertyName).endsWith("businesslegalname");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("company", "companyName", "company-name", "company_name",
                "businessName", "business-name", "business_name",
                "businessLegalName", "business-legal-name", "business_legal_name");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.faker().company().name();

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}