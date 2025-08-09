package dev.dochia.cli.core.model;

import dev.dochia.cli.core.util.KeyValuePair;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Model class used to stored request details.
 */
@Getter
@Setter
@Builder
public class HttpRequest {
    List<KeyValuePair<String, Object>> headers;
    String payload;
    String httpMethod;
    String url;

    @Builder.Default
    String timestamp = DateTimeFormatter.RFC_1123_DATE_TIME.format(OffsetDateTime.now());

    /**
     * Creates an empty HttpRequest with placeholder values.
     * The generated request has an empty JSON payload, an undefined HTTP method ("####"),
     * an undefined URL ("####"), and an empty list of headers.
     *
     * @return An empty HttpRequest instance.
     */
    public static HttpRequest empty() {
        HttpRequest request = HttpRequest.builder().build();
        request.payload = "{}";
        request.httpMethod = "####";
        request.url = "####";
        request.headers = Collections.emptyList();
        return request;
    }
}
