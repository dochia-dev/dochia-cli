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
 * A generator class implementing interfaces for generating valid and invalid order number data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class OrderNumberGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        // Generate order number in e-commerce format
        int format = CommonUtils.random().nextInt(3);

        return switch (format) {
            case 0 -> "ORD-" + System.currentTimeMillis(); // ORD-1234567890123
            case 1 -> // Amazon-style: 123-1234567-1234567
                    String.format("%03d-%07d-%07d",
                            CommonUtils.random().nextInt(1000),
                            CommonUtils.random().nextInt(10000000),
                            CommonUtils.random().nextInt(10000000));
            default -> {
                // Alphanumeric: AB12CD34EF56
                String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                StringBuilder order = new StringBuilder();
                for (int i = 0; i < 12; i++) {
                    order.append(chars.charAt(CommonUtils.random().nextInt(chars.length())));
                }
                yield order.toString();
            }
        };
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "order".equalsIgnoreCase(format) ||
                "ordernumber".equalsIgnoreCase(format) ||
                (sanitized.contains("order") && (sanitized.contains("number") || sanitized.contains("id")));
    }

    @Override
    public String getAlmostValidValue() {
        // Too short
        return "ORD-123";
    }

    @Override
    public String getTotallyWrongValue() {
        return "order";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("order", "order-number", "ordernumber");
    }
}
