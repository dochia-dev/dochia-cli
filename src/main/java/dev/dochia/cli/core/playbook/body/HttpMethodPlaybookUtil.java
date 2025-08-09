package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.report.TestCaseListener;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Utility class executing common logic for playbooks sending undocumented HTTP methods.
 */
@Singleton
public class HttpMethodPlaybookUtil {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(HttpMethodPlaybookUtil.class);
    private final TestCaseListener testCaseListener;

    private final SimpleExecutor simpleExecutor;

    /**
     * Creates a new HttpMethodPlaybookUtil instance.
     *
     * @param tcl the test case listener
     * @param se  the executor
     */
    @Inject
    public HttpMethodPlaybookUtil(TestCaseListener tcl, SimpleExecutor se) {
        this.testCaseListener = tcl;
        this.simpleExecutor = se;
    }

    /**
     * Processes fuzzing for a specific HTTP method using the provided playbook and PlaybookData.
     *
     * <p>This method utilizes a SimpleExecutor to execute fuzzing based on the given playbook, PlaybookData,
     * and HTTP method. It configures the execution context with the necessary parameters, including the logger,
     * expected response code, payload, scenario description, response processor, and additional fuzzing-related details.</p>
     *
     * @param testCasePlaybook     The Playbook instance responsible for generating test cases and payloads during fuzzing.
     * @param data       The FuzzingData containing information about the path, method, payload, and headers.
     * @param httpMethod The HTTP method for which fuzzing is being performed.
     */
    public void process(TestCasePlaybook testCasePlaybook, PlaybookData data, HttpMethod httpMethod) {
        simpleExecutor.execute(
                SimpleExecutorContext.builder()
                        .logger(logger)
                        .testCasePlaybook(testCasePlaybook)
                        .expectedSpecificResponseCode("405")
                        .payload(HttpMethod.requiresBody(httpMethod) ? data.getPayload() : "")
                        .scenario("Send a happy flow request with undocumented HTTP method: %s".formatted(httpMethod))
                        .responseProcessor(this::checkResponse)
                        .playbookData(data)
                        .httpMethod(httpMethod)
                        .build()
        );
    }

    private void checkResponse(HttpResponse response, PlaybookData data) {
        if (response.getResponseCode() == 405) {
            testCaseListener.reportResultInfo(logger, data, "Request failed as expected for http method [{}] with response code [{}]",
                    response.getHttpMethod(), response.getResponseCode());
        } else if (ResponseCodeFamily.is2xxCode(response.getResponseCode())) {
            testCaseListener.reportResultError(logger, data, "Unexpected response code: %s".formatted(response.getResponseCode()), "Request succeeded unexpectedly for http method [{}]: expected [{}], actual [{}]",
                    response.getHttpMethod(), 405, response.getResponseCode());
        } else {
            testCaseListener.reportResultWarn(logger, data, "Unexpected response code: %s".formatted(response.getResponseCode()), "Unexpected response code for http method [{}]: expected [{}], actual [{}]",
                    response.getHttpMethod(), 405, response.getResponseCode());
        }
    }
}
