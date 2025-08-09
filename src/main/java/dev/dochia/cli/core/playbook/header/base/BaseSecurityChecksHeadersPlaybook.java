package dev.dochia.cli.core.playbook.header.base;

import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.generator.Cloner;
import dev.dochia.cli.core.generator.simple.StringGenerator;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Base class used to send different values in Accept and Content-Type headers.
 */
public abstract class BaseSecurityChecksHeadersPlaybook implements TestCasePlaybook {
    /**
     * Constant for a dochia specific Accept header.
     */
    protected static final String DOCHIA_ACCEPT = "application/dochia";


    private final PrettyLogger log = PrettyLoggerFactory.getLogger(this.getClass());

    private final SimpleExecutor simpleExecutor;

    /**
     * Creates a new instance of subclass.
     *
     * @param simpleExecutor the executor
     */
    protected BaseSecurityChecksHeadersPlaybook(SimpleExecutor simpleExecutor) {
        this.simpleExecutor = simpleExecutor;
    }

    /**
     * Removes content types defined in the contract from the {@code UNSUPPORTED_MEDIA_TYPES} list.
     * Content Types not already defined in contract should be treated as invalid by the service.
     *
     * @param data         the current FuzzingData
     * @param headerName   either Accept or Content-Type
     * @param contentTypes Content-Type headers supported by the contract
     * @return a list of set of headers that will be sent in independent requests
     */
    protected static List<Set<DochiaHeader>> filterHeaders(PlaybookData data, String headerName, List<String> contentTypes) {
        List<Set<DochiaHeader>> setOfSets = new ArrayList<>();

        for (String currentHeader : StringGenerator.getUnsupportedMediaTypes()) {
            if (contentTypes.stream().noneMatch(currentHeader::startsWith)) {
                Set<DochiaHeader> clonedHeaders = Cloner.cloneMe(data.getHeaders());
                clonedHeaders.add(DochiaHeader.builder().name(headerName).value(currentHeader).build());
                setOfSets.add(clonedHeaders);
            }
        }
        return setOfSets;
    }

    @Override
    public void run(PlaybookData data) {
        for (Set<DochiaHeader> headers : this.getHeaders(data)) {
            String headerValue = headers.stream().filter(header -> header.getName().equalsIgnoreCase(targetHeaderName()))
                    .findFirst().orElse(DochiaHeader.builder().build()).getValue();
            simpleExecutor.execute(
                    SimpleExecutorContext.builder()
                            .scenario("Send a happy flow request with a [%s] %s header, value [%s]".formatted(typeOfHeader(), targetHeaderName(), headerValue))
                            .logger(log)
                            .playbookData(data)
                            .testCasePlaybook(this)
                            .expectedResponseCode(this.getResponseCodeFamily())
                            .expectedSpecificResponseCode(this.getExpectedResponseCode())
                            .matchResponseResult(false)
                            .matchResponseContentType(this.shouldMatchContentType())
                            .headers(headers)
                            .build());
        }
    }

    /**
     * When sending large or malformed values, the payload might not reach the application layer, but rather be rejected by the HTTP server.
     * In those cases, response content-type is typically html which will most likely won't match the OpenAPI spec.
     * <p>
     * Override this to return false to avoid content type checking.
     *
     * @return true if the playbook should check if the response content type matches the contract, false otherwise
     */
    protected boolean shouldMatchContentType() {
        return true;
    }

    /**
     * What is the expected list of response codes.
     *
     * @return an HTTP response code list
     */
    public abstract ResponseCodeFamily getResponseCodeFamily();

    /**
     * What is the expected response code.
     *
     * @return an HTTP expected response code
     */
    public abstract String getExpectedResponseCode();

    /**
     * Short description of the type of data sent within the headers.
     *
     * @return a short description
     */
    public abstract String typeOfHeader();

    /**
     * The name of the targeted header.
     *
     * @return the name of the header
     */
    public abstract String targetHeaderName();

    /**
     * A list of HTTP headers sets that will be used to create test cases. dochia will create a test case for each Set.
     *
     * @param data the current FuzzingData
     * @return a list of header sets
     */
    public abstract List<Set<DochiaHeader>> getHeaders(PlaybookData data);

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public String description() {
        return String.format("send a request with a %s %s header and expect to get %s code", typeOfHeader(), targetHeaderName(), getExpectedResponseCode());
    }
}
