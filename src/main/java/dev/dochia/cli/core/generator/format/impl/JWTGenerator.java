package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormatGenerator;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.generator.format.api.PropertySanitizer;
import dev.dochia.cli.core.generator.format.api.ValidDataFormatGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * A generator class implementing interfaces for generating valid and invalid JWT (JSON Web Token) data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class JWTGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    @Override
    public Object generate(Schema<?> schema) {
        // Generate a fake JWT structure: header.payload.signature
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(("{\"sub\":\"" + CommonUtils.faker().internet().uuid() + 
                        "\",\"name\":\"" + CommonUtils.faker().name().fullName() + 
                        "\",\"iat\":" + (System.currentTimeMillis() / 1000) + "}").getBytes(StandardCharsets.UTF_8));
        
        // Generate a fake signature (random bytes)
        byte[] signatureBytes = new byte[32];
        CommonUtils.random().nextBytes(signatureBytes);
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
        
        return header + "." + payload + "." + signature;
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "jwt".equalsIgnoreCase(format) ||
                sanitized.contains("jwt") ||
                sanitized.contains("jsonwebtoken") ||
                (sanitized.endsWith("token") && (sanitized.contains("auth") || sanitized.contains("bearer")));
    }

    @Override
    public String getAlmostValidValue() {
        // Missing signature part
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ";
    }

    @Override
    public String getTotallyWrongValue() {
        return "not.a.valid.jwt.token";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("jwt", "jsonwebtoken", "bearer-token");
    }
}
