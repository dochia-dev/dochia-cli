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
 * Generates real bank account numbers.
 */
@Singleton
public class BankAccountNumberGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    private static final String[] FORMATS = new String[]{"## ## ## ##", "## ## ## ## ##"};

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "accountnumber".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("bankaccountnumber") ||
                PropertySanitizer.sanitize(propertyName).endsWith("bankaccountaccountnumber");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("account-number", "accountNumber");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.faker().numerify(FORMATS[CommonUtils.random().nextInt(FORMATS.length)]);

        return DataFormat.matchesPatternOrNullWithCombinations(schema, generated);
    }
}