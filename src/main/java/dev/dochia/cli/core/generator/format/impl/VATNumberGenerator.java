package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * A generator class implementing interfaces for generating valid and invalid VAT number data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class VATNumberGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    private static final List<String> COUNTRY_CODES = List.of("GB", "DE", "FR", "IT", "ES", "NL", "BE", "AT", "IE", "PT");

    @Override
    public Object generate(Schema<?> schema) {
        // Generate EU VAT format: CC123456789
        String countryCode = CommonUtils.selectRandom(COUNTRY_CODES);
        int length = 8 + CommonUtils.random().nextInt(3); // 8-10 digits
        StringBuilder number = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            number.append(CommonUtils.random().nextInt(10));
        }
        
        return countryCode + number.toString();
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "vat".equalsIgnoreCase(format) ||
                "vatnumber".equalsIgnoreCase(format) ||
                sanitized.contains("vat") ||
                (sanitized.contains("taxnumber") && !sanitized.contains("taxid"));
    }

    @Override
    public String getAlmostValidValue() {
        // Invalid country code
        return "XX123456789";
    }

    @Override
    public String getTotallyWrongValue() {
        return "VAT123";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("vat", "vat-number", "vatnumber");
    }
}
