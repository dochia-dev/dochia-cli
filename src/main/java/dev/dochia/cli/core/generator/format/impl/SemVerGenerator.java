package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.DataFormat;
import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.DochiaRandom;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A generator class implementing interfaces for generating valid and invalid semantic version data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class SemVerGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        int major = CommonUtils.random().nextInt(10);
        int minor = CommonUtils.random().nextInt(20);
        int patch = CommonUtils.random().nextInt(50);

        List<String> generated = new ArrayList<>();
        generated.add(String.format("%d.%d.%d-alpha.%d", major, minor, patch, CommonUtils.random().nextInt(10)));
        generated.add(String.format("%d.%d.%d-beta.%d", major, minor, patch, CommonUtils.random().nextInt(10)));
        generated.add(String.format("%d.%d.%d+build.%d", major, minor, patch, CommonUtils.random().nextInt(1000)));
        generated.add(String.format("%d.%d.%d", major, minor, patch));

        Collections.shuffle(generated, DochiaRandom.instance());

        return DataFormat.matchesPatternOrNullFromList(schema, generated);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "semver".equalsIgnoreCase(format) ||
                "semanticversion".equalsIgnoreCase(format) ||
                "version".equalsIgnoreCase(format) ||
                sanitized.contains("semver") ||
                sanitized.contains("semanticversion") ||
                sanitized.endsWith("apiversion") ||
                sanitized.endsWith("appversion") ||
                sanitized.endsWith("softwareversion");
    }

    @Override
    public String getAlmostValidValue() {
        // Missing patch version
        return "1.2";
    }

    @Override
    public String getTotallyWrongValue() {
        return "v1.2.3.4.5";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("semver", "semantic-version", "version");
    }
}
