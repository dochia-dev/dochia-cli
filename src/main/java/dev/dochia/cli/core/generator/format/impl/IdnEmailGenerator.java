package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Locale;

/**
 * A generator class implementing interfaces for generating valid IDN (Internationalized Domain Name) email data formats.
 * It implements the ValidDataFormatGenerator and OpenAPIFormat interfaces.
 */
@Singleton
public class IdnEmailGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    @Override
    public Object generate(Schema<?> schema) {
        return CommonUtils.faker().ancient().primordial().toLowerCase(Locale.ROOT) + ".cööl.dochia@dochia.dev";
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "idn-email".equalsIgnoreCase(format);
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("idn-email");
    }
}
