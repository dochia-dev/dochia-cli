package dev.dochia.cli.core.playbook.executor;

import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Context used by the SimpleExecutor to execute fuzzing logic.
 */
@Builder
@Value
public class SimpleExecutorContext {

    /**
     * Playbook's logger.
     */
    PrettyLogger logger;

    @NonNull
    PlaybookData playbookData;

    String scenario;
    /**
     * This is used to match against the response code received from the Service.
     */
    ResponseCodeFamily expectedResponseCode;
    /**
     * You can supply this in order to be more specific in the expected response code section of the report. For example: {@code Should return 406}
     * If this is null, the report will consider {@code expectedResponseCode.asString()}. This will look like: {@code Should return 4XX}.
     */
    String expectedSpecificResponseCode;

    TestCasePlaybook testCasePlaybook;

    /**
     * Whether to replace ref data. Some Playbooks will not want ref data to be replaced.
     */
    @Builder.Default
    boolean replaceRefData = true;

    /**
     * Marks if a valid JSON is sent as payload.
     * This is useful for Playbooks that send invalid JSON payloads.
     */
    @Builder.Default
    boolean validJson = true;

    /**
     * Any headers that will get removed before calling the service.
     */
    @Builder.Default
    Set<String> skippedHeaders = new HashSet<>();

    /**
     * A modified payload, other than the one from {@code FuzzingData#payload}. If this is null, the
     * Executor will call {@code fuzzingData.getPayload()}.
     */
    String payload;

    /**
     * You can override the list of headers by populating the {@code headers} field.
     * This is useful when adding additional headers on top of the one supplied inside3 the API specs.
     */
    Collection<DochiaHeader> headers;

    /**
     * A custom response processor. The Executor only calls {@code TestCaseListener#reportResult}. If more complex logic is needed
     * to process the response and decide the expected behaviours, you can supply your own processor.
     */
    BiConsumer<HttpResponse, PlaybookData> responseProcessor;

    /**
     * Whether to add the headers supplied in the {@code --headers} file.
     */
    @Builder.Default
    boolean addUserHeaders = true;

    /**
     * Whether to replace the path variables with the {@code --urlParams} supplied.
     */
    @Builder.Default
    boolean replaceUrlParams = true;

    /**
     * If populated, this will get appended after the "Should return XXX" text.
     */
    @Builder.Default
    String expectedResult = "";

    HttpMethod httpMethod;

    /**
     * You can override this to provide your own path.
     */
    String path;

    @Builder.Default
    boolean matchResponseResult = true;

    /**
     * When sending large or malformed values the payload might not reach the application layer, but rather be rejected by the HTTP server.
     * In those cases response content-type is typically html which will most likely won't match the OpenAPI spec.
     * <p>
     * Override this to return false to avoid content type checking.
     */
    @Builder.Default
    boolean matchResponseContentType = true;

    /**
     * Gets the path. If the path is not set, it returns the path from the associated fuzzing data.
     *
     * @return The path, or the path from the associated fuzzing data if the path is not set.
     */
    public String getPath() {
        if (path == null) {
            return playbookData.getPath();
        }

        return path;
    }

    /**
     * If {@code httpMethod} is null, it will return the {@code fuzzingData.getHttpMethod()}.
     *
     * @return the http method to be executed
     */
    public HttpMethod getHttpMethod() {
        if (httpMethod == null) {
            return playbookData.getMethod();
        }
        return httpMethod;
    }

    /**
     * If the {@code expectedSpecificResponseCode} is null, this method will return {@code expectedResponseCode.asString()}.
     *
     * @return the expected response code to match against
     */
    public String getExpectedSpecificResponseCode() {
        if (expectedSpecificResponseCode == null) {
            return expectedResponseCode.asString();
        }
        return expectedSpecificResponseCode;
    }

    /**
     * If {@code headers} is null, it will return {@code fuzzingData.getHeaders()}.
     *
     * @return a list of headers to be sent along with the request
     */
    public Collection<DochiaHeader> getHeaders() {
        if (headers == null) {
            return playbookData.getHeaders();
        }
        return headers;
    }

    /**
     * A modified payload, other than the one from {@code FuzzingData#payload}. If this is null, the
     * Executor will call {@code fuzzingData.getPayload()}.
     *
     * @return the payload to be sent to the service
     */
    public String getPayload() {
        if (payload == null) {
            return playbookData.getPayload();
        }

        return payload;
    }
}
