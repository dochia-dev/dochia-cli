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
 * A generator class implementing interfaces for generating valid and invalid MIME type data formats.
 * It also implements the OpenAPIFormat interface.
 */
@Singleton
public class MimeTypeGenerator implements ValidDataFormatGenerator, InvalidDataFormatGenerator, OpenAPIFormat {

    private static final List<String> COMMON_MIME_TYPES = List.of(
            "application/json",
            "application/xml",
            "application/pdf",
            "application/zip",
            "application/octet-stream",
            "text/plain",
            "text/html",
            "text/css",
            "text/javascript",
            "text/csv",
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/svg+xml",
            "image/webp",
            "audio/mpeg",
            "audio/wav",
            "video/mp4",
            "video/webm",
            "multipart/form-data"
    );

    @Override
    public Object generate(Schema<?> schema) {
        return CommonUtils.selectRandom(COMMON_MIME_TYPES);
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        String sanitized = PropertySanitizer.sanitize(propertyName);
        return "mimetype".equalsIgnoreCase(format) ||
                "mime".equalsIgnoreCase(format) ||
                sanitized.contains("mimetype") ||
                sanitized.contains("mediatype") ||
                sanitized.endsWith("mime");
    }

    @Override
    public String getAlmostValidValue() {
        // Missing subtype
        return "application/";
    }

    @Override
    public String getTotallyWrongValue() {
        return "not-a-mime-type";
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("mimetype", "mime-type", "mime", "media-type");
    }
}
