package dev.dochia.cli.core.playbook.executor;

import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.io.ServiceData;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.report.TestCaseListener;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Executors are meant to be the units that will execute the playbook's payloads and facilitate the interaction with all the reporting.
 * <p>
 * <b>
 * They are not responsible for the creation of the payloads, but only for making sure the tests are executed within a testing context
 * and results are reported accordingly.
 * </b>
 * </p>
 * <p>
 * {@code SimpleExecutor} is the simplest form of executors. It will take the data from the Playbook and execute it, without any additional logic.
 * This is useful when Playbooks only create one interaction with the service.
 * </p>
 * <p>
 * You can control how reporting is done using your own {@code SimpleExecutorContext#responseProcessor}. By default, the Executor will call
 * {@code TestCaseListener#reportResult} matching against the supplied {@code SimpleExecutorContext#expectedResponseCode}. You can also supply
 * a {@code SimpleExecutorContext#runFilter} if you want to skip reporting for specific HTTP methods.
 * </p>
 */
@ApplicationScoped
public class SimpleExecutor {
    private final TestCaseListener testCaseListener;
    private final ServiceCaller serviceCaller;

    /**
     * Constructs a new instance of SimpleExecutor.
     *
     * <p>This executor is designed to simplify the execution of test cases by relying on a provided
     * TestCaseListener for handling events and a ServiceCaller for making service calls during execution.</p>
     *
     * @param testCaseListener The TestCaseListener instance responsible for handling test case events.
     * @param serviceCaller    The ServiceCaller instance responsible for making service calls during execution.
     */
    @Inject
    public SimpleExecutor(TestCaseListener testCaseListener, ServiceCaller serviceCaller) {
        this.testCaseListener = testCaseListener;
        this.serviceCaller = serviceCaller;
    }

    /**
     * Executes the logic considering the given context.
     * This method will do the actual HTTP call to the service and match the response against expected behaviour.
     *
     * @param context the executor context
     */
    public void execute(SimpleExecutorContext context) {
        testCaseListener.createAndExecuteTest(context.getLogger(), context.getTestCasePlaybook(), () -> {
            testCaseListener.addScenario(context.getLogger(), context.getScenario());
            testCaseListener.addExpectedResult(context.getLogger(), "Should return {}" + context.getExpectedResult(), context.getExpectedSpecificResponseCode());

            HttpResponse response = serviceCaller.call(
                    ServiceData.builder()
                            .relativePath(context.getPath())
                            .contractPath(context.getPlaybookData().getContractPath())
                            .headers(context.getHeaders())
                            .payload(context.getPayload())
                            .queryParams(context.getPlaybookData().getQueryParams())
                            .httpMethod(context.getHttpMethod())
                            .contentType(context.getPlaybookData().getFirstRequestContentType())
                            .replaceRefData(context.isReplaceRefData())
                            .skippedHeaders(context.getSkippedHeaders())
                            .addUserHeaders(context.isAddUserHeaders())
                            .replaceUrlParams(context.isReplaceUrlParams())
                            .validJson(context.isValidJson())
                            .pathParamsPayload(context.getPlaybookData().getPathParamsPayload())
                            .build());

            if (context.getResponseProcessor() != null) {
                context.getResponseProcessor().accept(response, context.getPlaybookData());
            } else {
                testCaseListener.reportResult(context.getLogger(), context.getPlaybookData(), response, context.getExpectedResponseCode(), context.isMatchResponseResult(), context.isMatchResponseContentType());
            }
        }, context.getPlaybookData());
    }
}
