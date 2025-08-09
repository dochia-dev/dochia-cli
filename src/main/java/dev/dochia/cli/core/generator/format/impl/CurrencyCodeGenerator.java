package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A generator class implementing various interfaces for generating valid and invalid currency code data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class CurrencyCodeGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        Set<Currency> currencySet = Currency.getAvailableCurrencies();
        return currencySet.stream().skip(CommonUtils.random().nextInt(currencySet.size())).findFirst().orElse(Currency.getInstance(Locale.UK)).getCurrencyCode();
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "currencycode".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                "iso4217".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("currencycode") ||
                PropertySanitizer.sanitize(propertyName).endsWith("currency");
    }

    @Override
    public String getAlmostValidValue() {
        return "ROL";
    }

    @Override
    public String getTotallyWrongValue() {
        return "XXX";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("iso4217", "currencyCode", "currency-code", "currency_code");
    }
}
