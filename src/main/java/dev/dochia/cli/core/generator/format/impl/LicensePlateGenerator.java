package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.DataFormat;
import dev.dochia.cli.core.generator.format.api.FormatGeneratorUtil;
import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

/**
 * A generator class implementing interfaces for generating valid and invalid license plate data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class LicensePlateGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {


    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "licenseplate".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                "numberplate".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("licenseplate") ||
                PropertySanitizer.sanitize(propertyName).endsWith("numberplate") ||
                PropertySanitizer.sanitize(propertyName).endsWith("plateNumber") ||
                PropertySanitizer.sanitize(propertyName).endsWith("platenumber");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("licensePlate", "license-plate", "license_plate", "numberPlate", "number-plate", "number_plate");
    }

    @Override
    public Object generate(Schema<?> schema) {
        List<String> candidates = new ArrayList<>();

        // US formats
        candidates.add(FormatGeneratorUtil.generateFromPattern("###-AAA"));
        candidates.add(FormatGeneratorUtil.generateFromPattern("AAA-###"));
        candidates.add(FormatGeneratorUtil.generateFromPattern("AA##-AAA"));

        // UK format: AA## AAA
        candidates.add(FormatGeneratorUtil.generateFromPattern("AA## AAA"));

        // German format: AB-CD 1234
        candidates.add(FormatGeneratorUtil.generateFromPattern("AA-AA ####"));

        // French format: AB-123-CD
        candidates.add(FormatGeneratorUtil.generateFromPattern("AA-###-AA"));

        // Italian format: AB 123 CD
        candidates.add(FormatGeneratorUtil.generateFromPattern("AA ### AA"));

        // Spanish format: 1234 ABC
        candidates.add(FormatGeneratorUtil.generateFromPattern("#### AAA"));

        // Dutch format: AB-123-C
        candidates.add(FormatGeneratorUtil.generateFromPattern("AA-###-A"));

        // Romanian format: CJ 99 BUG
        candidates.add(FormatGeneratorUtil.generateFromPattern("AA ## AAA"));

        return DataFormat.matchesPatternOrNullFromList(schema, candidates);
    }

    @Override
    public String getAlmostValidValue() {
        // Too many characters
        return "ABCD-12345";
    }

    @Override
    public String getTotallyWrongValue() {
        return "plate";
    }
}
