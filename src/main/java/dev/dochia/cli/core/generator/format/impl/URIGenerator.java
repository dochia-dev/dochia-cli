package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Locale;

/**
 * A generator class implementing interfaces for generating valid and invalid URI (Uniform Resource Identifier) data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class URIGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {
    private static final String URL = "url";
    private static final String URI = "uri";
    private static final String LINK = "link";

    @Override
    public Object generate(Schema<?> schema) {
        String hero = CommonUtils.faker().ancient().hero().toLowerCase(Locale.ROOT);
        String color = CommonUtils.faker().color().name().toLowerCase(Locale.ROOT);
        return "https://%s-%s.com/dochia".formatted(hero, color).replace(" ", "-");
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return URI.equals(format) || URL.equals(format)
                || propertyName.toLowerCase(Locale.ROOT).endsWith(URL)
                || propertyName.toLowerCase(Locale.ROOT).endsWith(URI)
                || propertyName.toLowerCase(Locale.ROOT).endsWith(LINK);
    }

    @Override
    public String getAlmostValidValue() {
        return "http://dochiaiscool.";
    }

    @Override
    public String getTotallyWrongValue() {
        return "dochiaiscool";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("url", "uri");
    }
}
