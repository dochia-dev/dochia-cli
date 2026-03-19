package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.DataFormat;
import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.DochiaRandom;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * A generator class implementing interfaces for generating valid and invalid medical record number data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class MedicalRecordNumberGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "mrn".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                "medicalrecordnumber".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("mrn") ||
                PropertySanitizer.sanitize(propertyName).endsWith("medicalrecordnumber") ||
                PropertySanitizer.sanitize(propertyName).endsWith("patientid") ||
                PropertySanitizer.sanitize(propertyName).endsWith("patientnumber");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("mrn", "medicalRecordNumber", "medical-record-number", "medical_record_number", "patient-id");
    }

    public MedicalRecordNumberGenerator() {
    }

    @Override
    public Object generate(Schema<?> schema) {
        int part1 = DochiaRandom.instance().nextInt(900) + 100;
        int part2 = DochiaRandom.instance().nextInt(900000) + 100000;

        String generated = String.format("%03d-%06d", part1, part2);

        return DataFormat.matchesPatternOrNullWithCombinations(schema, generated);
    }

    @Override
    public String getAlmostValidValue() {
        // Too short
        return "MRN-123";
    }

    @Override
    public String getTotallyWrongValue() {
        return "patient";
    }
}
