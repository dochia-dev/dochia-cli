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
 * A generator class implementing interfaces for generating valid and invalid medical record number data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class MedicalRecordNumberGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        // Generate MRN in common healthcare formats
        int format = CommonUtils.random().nextInt(3);

        return switch (format) {
            case 0 -> // Format: MRN-1234567
                    "MRN-" + String.format("%07d", CommonUtils.random().nextInt(10000000));
            case 1 -> // Format: 12345678 (8 digits)
                    String.format("%08d", CommonUtils.random().nextInt(100000000));
            default -> {
                // Format: A12345678 (letter + 8 digits)
                String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                yield letters.charAt(CommonUtils.random().nextInt(letters.length())) +
                        String.format("%08d", CommonUtils.random().nextInt(100000000));
            }
        };
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "mrn".equalsIgnoreCase(format) ||
                "medicalrecordnumber".equalsIgnoreCase(format) ||
                sanitized.contains("mrn") ||
                sanitized.contains("medicalrecord") ||
                (sanitized.contains("patientid") && !sanitized.contains("external"));
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

    @Override
    public List<String> matchingFormats() {
        return List.of("mrn", "medical-record-number", "medicalrecordnumber", "patient-id");
    }
}
