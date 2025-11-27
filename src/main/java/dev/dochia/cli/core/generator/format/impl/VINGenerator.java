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
 * A generator class implementing interfaces for generating valid and invalid VIN (Vehicle Identification Number) data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class VINGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        // Generate 17-character VIN (excluding I, O, Q to avoid confusion)
        String chars = "ABCDEFGHJKLMNPRSTUVWXYZ0123456789";
        StringBuilder vin = new StringBuilder();
        
        for (int i = 0; i < 17; i++) {
            vin.append(chars.charAt(CommonUtils.random().nextInt(chars.length())));
        }
        
        return vin.toString();
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "vin".equalsIgnoreCase(format) ||
                sanitized.contains("vin") ||
                sanitized.contains("vehicleidentificationnumber") ||
                sanitized.contains("chassisnumber");
    }

    @Override
    public String getAlmostValidValue() {
        // Contains invalid character 'O'
        return "1HGBH41JXMN1O9186";
    }

    @Override
    public String getTotallyWrongValue() {
        return "VIN123";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("vin", "vehicle-identification-number", "chassis-number");
    }
}
