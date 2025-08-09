package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * A generator class implementing various interfaces for generating valid and invalid data formats
 * based on BCP 47 language tags. It also implements the OpenAPIFormat interface.
 */
@Singleton
public class Bcp47Generator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {
    @Override
    public Object generate(Schema<?> schema) {
        String[] locales = {"en-US", "en-JP", "fr-FR", "de-DE", "de-CH", "de-JP", "ro-RO"};
        return locales[CommonUtils.random().nextInt(locales.length)];
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "bcp47".equalsIgnoreCase(format);
    }

    @Override
    public String getAlmostValidValue() {
        return "ro-US";
    }

    @Override
    public String getTotallyWrongValue() {
        return "xx-XX";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("bcp47");
    }
}
