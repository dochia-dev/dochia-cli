package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;

/**
 * A generator class implementing various interfaces for generating valid and invalid hostname data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class HostnameGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {
    @Override
    public Object generate(Schema<?> schema) {
        String generated = RandomStringUtils.secure().nextAlphabetic(5);
        return "www.dochia%s.dev".formatted(generated);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "hostname".equalsIgnoreCase(format);
    }

    @Override
    public String getAlmostValidValue() {
        return "cool.dochia.";
    }

    @Override
    public String getTotallyWrongValue() {
        return "aaa111-aaaaa---";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("hostname");
    }
}
