package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A generator class implementing interfaces for generating valid and invalid MAC address data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class MACAddressGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        // Generate MAC address in format XX:XX:XX:XX:XX:XX
        return IntStream.range(0, 6)
                .mapToObj(i -> String.format("%02X", CommonUtils.random().nextInt(256)))
                .collect(Collectors.joining(":"));
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "mac".equalsIgnoreCase(format) ||
                "macaddress".equalsIgnoreCase(format) ||
                sanitized.contains("macaddress") ||
                (sanitized.endsWith("mac") && !sanitized.contains("stomach"));
    }

    @Override
    public String getAlmostValidValue() {
        // Invalid hex character 'G'
        return "00:1G:22:33:44:55";
    }

    @Override
    public String getTotallyWrongValue() {
        return "not-a-mac";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("mac", "mac-address", "macaddress");
    }
}
