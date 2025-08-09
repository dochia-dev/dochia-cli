package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static dev.dochia.cli.core.util.DochiaModelUtils.hasLengthThree;
import static dev.dochia.cli.core.util.DochiaModelUtils.hasLengthTwo;

/**
 * A generator class implementing various interfaces for generating valid and invalid country code data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class CountryCodeGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {
    @Override
    public Object generate(Schema<?> schema) {
        String[] isoCountries = Locale.getISOCountries();
        String randomCountry = Arrays.stream(Locale.getISOCountries())
                .skip(CommonUtils.random().nextInt(isoCountries.length))
                .findFirst()
                .orElse(Locale.UK.getCountry());

        Locale locale = new Locale.Builder().setLanguage("en").setRegion(randomCountry).build();

        if (hasLengthTwo(schema) || patternMatchesTwoLetterIsoCode(schema)) {
            return locale.getCountry();
        }
        if (hasLengthThree(schema) || patternMatchesThreeLetterIsoCode(schema)) {
            return locale.getISO3Country();
        }

        return locale.getDisplayCountry();
    }

    private static boolean patternMatchesTwoLetterIsoCode(Schema<?> schema) {
        return schema.getPattern() != null && "RO".matches(schema.getPattern());
    }

    private static boolean patternMatchesThreeLetterIsoCode(Schema<?> schema) {
        return schema.getPattern() != null && "ROU".matches(schema.getPattern());
    }


    @Override
    public boolean appliesTo(String format, String propertyName) {
        String[] parts = propertyName.split("#", 0);
        String lastPart = parts[parts.length - 1];
        return "iso3166".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                "countrycode".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(lastPart).endsWith("country") ||
                PropertySanitizer.sanitize(lastPart).startsWith("country");
    }

    @Override
    public String getAlmostValidValue() {
        return "ROM";
    }

    @Override
    public String getTotallyWrongValue() {
        return "XXX";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("iso3166", "countryCode", "country-code", "country_code", "country");
    }
}
