package dev.dochia.cli.core.playbook.stateful;

import dev.dochia.cli.core.context.GlobalContext;
import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.api.StatefulPlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Singleton;

import java.net.URI;
import java.util.List;

/**
 * Playbook that checks if deleted resources are still available.
 */
@BodyPlaybook
@StatefulPlaybook
@Singleton
public class DeletedResourcesNotAvailablePlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(DeletedResourcesNotAvailablePlaybook.class);
    private final SimpleExecutor simpleExecutor;
    private final GlobalContext globalContext;
    private final TestCaseListener testCaseListener;

    /**
     * Creates a new CheckDeletedResourcesNotAvailablePlaybook instance.
     *
     * @param simpleExecutor   the executor
     * @param globalContext    the  global context
     * @param testCaseListener the test case listener
     */
    public DeletedResourcesNotAvailablePlaybook(SimpleExecutor simpleExecutor, GlobalContext globalContext, TestCaseListener testCaseListener) {
        this.simpleExecutor = simpleExecutor;
        this.globalContext = globalContext;
        this.testCaseListener = testCaseListener;
    }

    @Override
    public void run(PlaybookData data) {
        if (data.getMethod() != HttpMethod.GET) {
            return;
        }

        logger.info("Stored successful DELETE requests: {}", globalContext.getSuccessfulDeletes().size());
        for (String delete : globalContext.getSuccessfulDeletes()) {
            simpleExecutor.execute(
                    SimpleExecutorContext.builder()
                            .logger(logger)
                            .testCasePlaybook(this)
                            .expectedResponseCode(ResponseCodeFamilyPredefined.FOURXX)
                            .playbookData(data)
                            .payload("{}")
                            .path(getRelativePath(delete))
                            .scenario("Check that previously deleted resource is not available")
                            .responseProcessor(this::checkResponse)
                            .build()
            );
        }
        globalContext.getSuccessfulDeletes().clear();
    }

    private void checkResponse(HttpResponse response, PlaybookData data) {
        if (response.getResponseCode() == 404 || response.getResponseCode() == 410) {
            testCaseListener.reportResultInfo(logger, data, "Request failed as expected for http method [{}] with response code [{}]",
                    response.getHttpMethod(), response.getResponseCode());
        } else {
            testCaseListener.reportResultError(logger, data, "Unexpected response code: %s".formatted(response.getResponseCode()), "Request succeeded unexpectedly for http method [{}]: expected [{}], actual [{}]",
                    data.getMethod(), "404, 410", response.responseCodeAsString());
        }
    }

    static String getRelativePath(String url) {
        try {
            return URI.create(url).toURL().getPath();
        } catch (Exception e) {
            return url;
        }
    }

    @Override
    public List<HttpMethod> skipForHttpMethods() {
        return List.of(HttpMethod.HEAD, HttpMethod.PATCH, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.TRACE);
    }

    @Override
    public String description() {
        return "checks that resources are not available after successful deletes";
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }
}
