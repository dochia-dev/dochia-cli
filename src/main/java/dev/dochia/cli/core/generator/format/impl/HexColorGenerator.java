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
 * A generator class implementing interfaces for generating valid and invalid hex color code data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class HexColorGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        // Generate random hex color
        int r = CommonUtils.random().nextInt(256);
        int g = CommonUtils.random().nextInt(256);
        int b = CommonUtils.random().nextInt(256);
        
        return String.format("#%02X%02X%02X", r, g, b);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "hexcolor".equalsIgnoreCase(format) ||
                "color".equalsIgnoreCase(format) ||
                sanitized.contains("hexcolor") ||
                sanitized.contains("colorhex") ||
                (sanitized.endsWith("color") && !sanitized.contains("multicolor"));
    }

    @Override
    public String getAlmostValidValue() {
        // Missing one hex digit
        return "#FF00F";
    }

    @Override
    public String getTotallyWrongValue() {
        return "red";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("hexcolor", "hex-color", "color");
    }
}
