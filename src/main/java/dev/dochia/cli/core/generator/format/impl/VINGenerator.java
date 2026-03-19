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
 * A generator class implementing interfaces for generating valid and invalid VIN (Vehicle Identification Number) data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class VINGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    private static final String VIN_CHARS = "ABCDEFGHJKLMNPRSTUVWXYZ0123456789";
    private static final int[] VIN_WEIGHTS = {8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final String VIN_CHECK_DIGITS = "0123456789X";

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "vin".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("vin") ||
                PropertySanitizer.sanitize(propertyName).endsWith("vehicleidentificationnumber") ||
                PropertySanitizer.sanitize(propertyName).endsWith("chassisnumber");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("vin", "vehicle-identification-number", "vehicle_identification_number");
    }

    @Override
    public Object generate(Schema<?> schema) {
        StringBuilder vin = new StringBuilder();

        for (int i = 0; i < 17; i++) {
            if (i == 8) {
                vin.append('0');
            } else {
                vin.append(VIN_CHARS.charAt(DochiaRandom.instance().nextInt(VIN_CHARS.length())));
            }
        }

        int checkDigit = calculateCheckDigit(vin.toString());
        vin.setCharAt(8, VIN_CHECK_DIGITS.charAt(checkDigit));

        String generated = vin.toString();

        return DataFormat.matchesPatternOrNull(schema, generated);
    }

    private int calculateCheckDigit(String vin) {
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += getCharValue(vin.charAt(i)) * VIN_WEIGHTS[i];
        }
        return sum % 11;
    }

    private int getCharValue(char c) {
        if (Character.isDigit(c)) {
            return c - '0';
        }
        String alphabet = "ABCDEFGHJKLMNPRSTUVWXYZ";
        int[] values = {1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 7, 9, 2, 3, 4, 5, 6, 7, 8, 9};
        int index = alphabet.indexOf(c);
        return index >= 0 ? values[index] : 0;
    }

    @Override
    public String getAlmostValidValue() {
        return "1HGBH41JXMN109186";
    }

    @Override
    public String getTotallyWrongValue() {
        return "INVALID123VIN456";
    }
}
