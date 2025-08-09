package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.generator.simple.StringGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Generates content types.
 */
@Singleton
public class ContentTypeGenerator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "contenttype".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("contenttype");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("contentType", "content-type");
    }

    @Override
    public Object generate(Schema<?> schema) {
        int generated = CommonUtils.random().nextInt(StringGenerator.getUnsupportedMediaTypes().size());

        return StringGenerator.getUnsupportedMediaTypes().get(generated);
    }
}