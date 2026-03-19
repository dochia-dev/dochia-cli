package dev.dochia.cli.core.generator.format.impl;


import dev.dochia.cli.core.generator.format.api.DataFormat;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.DochiaRandom;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Generates IP address ranges in CIDR notation.
 */
@Singleton
public class IPAddressRangeGenerator implements ValidDataFormatGenerator, OpenAPIFormat {

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "cidr".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                "iprange".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("cidr") ||
                PropertySanitizer.sanitize(propertyName).endsWith("iprange") ||
                PropertySanitizer.sanitize(propertyName).endsWith("subnet") ||
                PropertySanitizer.sanitize(propertyName).endsWith("networkrange");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("cidr", "ipRange", "ip-range", "ip_range", "subnet");
    }

    @Override
    public Object generate(Schema<?> schema) {
        int octet1 = DochiaRandom.instance().nextInt(224) + 1;
        int octet2 = DochiaRandom.instance().nextInt(256);
        int octet3 = DochiaRandom.instance().nextInt(256);
        int octet4 = 0;
        int cidrBits = DochiaRandom.instance().nextInt(9) + 16;

        String generated = String.format("%d.%d.%d.%d/%d", octet1, octet2, octet3, octet4, cidrBits);

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}
