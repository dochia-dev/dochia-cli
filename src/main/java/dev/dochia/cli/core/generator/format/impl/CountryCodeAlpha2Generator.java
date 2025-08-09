package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A generator class implementing various interfaces for generating valid and invalid country code (alpha-2) data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class CountryCodeAlpha2Generator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        Set<String> isoCountries = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2);
        return isoCountries.stream().skip(CommonUtils.random().nextInt(isoCountries.size())).findFirst().orElse(Locale.ROOT.getCountry());
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "iso3166alpha2".equalsIgnoreCase(PropertySanitizer.sanitize(format));
    }

    @Override
    public String getAlmostValidValue() {
        return "PP";
    }

    @Override
    public String getTotallyWrongValue() {
        return "XX";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("iso3166alpha2", "iso3166-alpha2", "iso3166_alpha2");
    }
}
