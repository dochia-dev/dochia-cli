package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.DataFormat;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Locale;

@Singleton
public class LanguageGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    private static final List<String> ALL_LANGUAGES = List.of(Locale.getISOCountries());

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "language".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("language");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("language");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CommonUtils.selectRandom(ALL_LANGUAGES);

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}
