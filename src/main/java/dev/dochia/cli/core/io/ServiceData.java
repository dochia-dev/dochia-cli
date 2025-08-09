package dev.dochia.cli.core.io;

import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.model.DochiaHeader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

/**
 * DTO holding all data needed to do a call to a service.
 */
@Builder
@Getter
public class ServiceData {
    private final String contractPath;
    private final String relativePath;
    private final Collection<DochiaHeader> headers;
    private final String payload;
    private final HttpMethod httpMethod;
    private final String contentType;
    private final String pathParamsPayload;
    @Builder.Default
    private final boolean replaceRefData = true;
    @Builder.Default
    private final boolean replaceUrlParams = true;
    @Builder.Default
    private final boolean validJson = true;
    /**
     * Set to true if headers supplied by the user will be added or false otherwise.
     * There are Playbooks which needs this level of control.
     */
    @Builder.Default
    private final boolean addUserHeaders = true;
    /**
     * Any header that will get removed before sending the request to the service.
     */
    @Builder.Default
    private final Set<String> skippedHeaders = new HashSet<>();
    @Singular
    private final Set<String> testedFields;
    @Singular
    private final Set<String> fuzzedHeaders;
    @Builder.Default
    private final Set<String> pathParams = new HashSet<>();
    @Builder.Default
    private final Set<String> queryParams = new HashSet<>();

    /**
     * Checks if the content type of the response is JSON.
     *
     * @return {@code true} if the content type matches the pattern "application/.*[+]?json;?.*", {@code false} otherwise.
     */
    public boolean isJsonContentType() {
        return this.contentType.toLowerCase(Locale.ROOT).matches("application/.*[+]?json;?.*");
    }
}
