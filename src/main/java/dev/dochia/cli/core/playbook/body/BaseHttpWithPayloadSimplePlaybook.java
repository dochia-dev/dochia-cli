package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.JsonUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import java.util.Arrays;
import java.util.List;

/**
 * This is a base class for Playbooks that want to send invalid payloads for HTTP methods accepting bodies.
 * It expects a 4XX within the response.
 */
public abstract class BaseHttpWithPayloadSimplePlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(getClass());
    private final SimpleExecutor simpleExecutor;

    BaseHttpWithPayloadSimplePlaybook(SimpleExecutor ce) {
        this.simpleExecutor = ce;
    }

    @Override
    public void run(PlaybookData data) {
        if (JsonUtils.isEmptyPayload(data.getPayload())) {
            logger.debug("Skipping playbook as payload is empty");
            return;
        }

        simpleExecutor.execute(
                SimpleExecutorContext.builder()
                        .expectedResponseCode(ResponseCodeFamilyPredefined.FOURXX)
                        .playbookData(data)
                        .logger(logger)
                        .replaceRefData(false)
                        .scenario(this.getScenario())
                        .testCasePlaybook(this)
                        .validJson(false)
                        .payload(this.getPayload(data))
                        .build()
        );
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    /**
     * Returns the scenario to be displayed in the test report.
     *
     * @return the test scenario
     */
    protected abstract String getScenario();

    /**
     * Returns the payload to be sent to the service.
     *
     * @param data the current FuzzingData
     * @return the payload to be sent to the service
     */
    protected abstract String getPayload(PlaybookData data);

    @Override
    public List<HttpMethod> skipForHttpMethods() {
        return Arrays.asList(HttpMethod.GET, HttpMethod.DELETE);
    }
}
