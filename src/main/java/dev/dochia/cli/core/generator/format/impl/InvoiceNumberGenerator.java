package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * A generator class implementing interfaces for generating valid and invalid invoice number data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class InvoiceNumberGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        // Generate invoice number in common formats
        int format = CommonUtils.random().nextInt(4);
        int year = LocalDate.now(ZoneId.systemDefault()).getYear();
        int sequence = 1000 + CommonUtils.random().nextInt(9000);
        
        return switch (format) {
            case 0 -> "INV-" + year + "-" + sequence; // INV-2024-1234
            case 1 -> "INV" + String.format("%06d", sequence); // INV001234
            case 2 -> year + "/" + sequence; // 2024/1234
            default -> String.format("%04d-%04d", year, sequence); // 2024-1234
        };
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "invoice".equalsIgnoreCase(format) ||
                "invoicenumber".equalsIgnoreCase(format) ||
                (sanitized.contains("invoice") && (sanitized.contains("number") || sanitized.contains("id")));
    }

    @Override
    public String getAlmostValidValue() {
        // Missing year
        return "INV-1234";
    }

    @Override
    public String getTotallyWrongValue() {
        return "invoice";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("invoice", "invoice-number", "invoicenumber");
    }
}
