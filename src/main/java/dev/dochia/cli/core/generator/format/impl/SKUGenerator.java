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
 * A generator class implementing interfaces for generating valid and invalid SKU (Stock Keeping Unit) data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class SKUGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        // Generate SKU in common formats
        int format = CommonUtils.random().nextInt(3);
        
        return switch (format) {
            case 0 -> {
                // Category-Brand-Product: ELEC-SONY-12345
                String[] categories = {"ELEC", "CLTH", "FOOD", "BOOK", "TOYS"};
                String[] brands = {"SONY", "NIKE", "APPL", "SAMS", "LEGO"};
                yield CommonUtils.selectRandom(List.of(categories)) + "-" +
                        CommonUtils.selectRandom(List.of(brands)) + "-" +
                        String.format("%05d", CommonUtils.random().nextInt(100000));
            }
            case 1 -> // Simple numeric with prefix: SKU-123456
                    "SKU-" + String.format("%06d", CommonUtils.random().nextInt(1000000));
            default -> {
                // Alphanumeric: AB12-CD34-EF56
                String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                StringBuilder sku = new StringBuilder();
                for (int i = 0; i < 12; i++) {
                    if (i > 0 && i % 4 == 0) {
                        sku.append("-");
                    }
                    sku.append(chars.charAt(CommonUtils.random().nextInt(chars.length())));
                }
                yield sku.toString();
            }
        };
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "sku".equalsIgnoreCase(format) ||
                sanitized.contains("sku") ||
                sanitized.contains("stockkeepingunit") ||
                sanitized.contains("productcode") ||
                sanitized.contains("itemcode");
    }

    @Override
    public String getAlmostValidValue() {
        // Invalid format (special characters)
        return "SKU@123#456";
    }

    @Override
    public String getTotallyWrongValue() {
        return "sku";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("sku", "product-code", "item-code");
    }
}
