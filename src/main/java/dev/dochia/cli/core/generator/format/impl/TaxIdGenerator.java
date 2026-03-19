package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.DataFormat;
import dev.dochia.cli.core.generator.format.api.FormatGeneratorUtil;
import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

/**
 * A generator class implementing interfaces for generating valid and invalid Tax ID/EIN data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class TaxIdGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(format);
        String sanitizedProperty = PropertySanitizer.sanitize(propertyName);

        return "taxid".equalsIgnoreCase(sanitized) ||
                "ein".equalsIgnoreCase(sanitized) ||
                "tin".equalsIgnoreCase(sanitized) ||
                "utr".equalsIgnoreCase(sanitized) ||
                "siren".equalsIgnoreCase(sanitized) ||
                "siret".equalsIgnoreCase(sanitized) ||
                "nif".equalsIgnoreCase(sanitized) ||
                sanitizedProperty.endsWith("taxid") ||
                sanitizedProperty.endsWith("taxidentificationnumber") ||
                sanitizedProperty.endsWith("ein") ||
                sanitizedProperty.endsWith("tin") ||
                sanitizedProperty.endsWith("utr") ||
                sanitizedProperty.endsWith("siren") ||
                sanitized.contains("taxpayerid") ||
                sanitized.contains("federaltaxid") ||
                sanitizedProperty.endsWith("siret") ||
                sanitizedProperty.endsWith("nif") ||
                sanitizedProperty.endsWith("codicefiscale") ||
                sanitizedProperty.endsWith("employeridentificationnumber");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("taxId", "tax-id", "tax_id", "ein", "tin", "utr", "siren", "siret", "nif");
    }

    @Override
    public Object generate(Schema<?> schema) {
        List<String> candidates = new ArrayList<>();

        // US EIN: 12-3456789
        candidates.add(String.format("%02d-%07d",
                FormatGeneratorUtil.randomNumber(2),
                FormatGeneratorUtil.randomNumber(7)));

        // UK UTR (Unique Taxpayer Reference): 10 digits
        candidates.add(FormatGeneratorUtil.randomDigits(10));

        // German Steuernummer: 12/345/67890
        candidates.add(String.format("%02d/%03d/%05d",
                FormatGeneratorUtil.randomNumber(2),
                FormatGeneratorUtil.randomNumber(3),
                FormatGeneratorUtil.randomNumber(5)));

        // French SIREN: 9 digits
        int frSiren = FormatGeneratorUtil.randomNumber(9);
        candidates.add(String.valueOf(frSiren));

        // French SIRET: 14 digits (SIREN + 5 digits)
        candidates.add(String.format("%d%05d", frSiren, FormatGeneratorUtil.randomNumber(5)));

        // Italian Codice Fiscale (simplified): 16 alphanumeric
        candidates.add(FormatGeneratorUtil.randomLetters(6) +
                FormatGeneratorUtil.randomDigits(2) +
                FormatGeneratorUtil.randomLetter() +
                FormatGeneratorUtil.randomDigits(2) +
                FormatGeneratorUtil.randomLetter() +
                FormatGeneratorUtil.randomDigits(3) +
                FormatGeneratorUtil.randomLetter());

        // Spanish NIF: 12345678A
        int esNumber = FormatGeneratorUtil.randomNumber(8);
        char esLetter = "TRWAGMYFPDXBNJZSQVHLCKE".charAt(esNumber % 23);
        candidates.add(String.format("%08d%c", esNumber, esLetter));

        return DataFormat.matchesPatternOrNullFromList(schema, candidates);
    }

    @Override
    public String getAlmostValidValue() {
        return CommonUtils.selectRandom(List.of(
                "00-1234567",       // US: invalid prefix
                "0000000000",       // UK: all zeros
                "00/000/00000",     // German: invalid format
                "000000000",        // French: all zeros
                "ABCDEF00A00A000Z"  // Italian: wrong format
        ));
    }

    @Override
    public String getTotallyWrongValue() {
        return CommonUtils.selectRandom(List.of(
                "123-45-6789",      // Wrong format
                "TAXID123",         // Not a valid format
                "12345",            // Too short
                "INVALID"           // Not a number
        ));
    }
}
