package dev.dochia.cli.core.playbook.executor;

import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.args.MatchArguments;
import dev.dochia.cli.core.generator.Cloner;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.io.ServiceData;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Executor used to execute logic when fuzzing headers.
 */
@Singleton
public class HeadersIteratorExecutor {

    private final ServiceCaller serviceCaller;
    private final TestCaseListener testCaseListener;
    private final MatchArguments matchArguments;
    private final FilterArguments filterArguments;

    /**
     * Creates a new HeadersIteratorExecutor instance.
     *
     * @param serviceCaller    the service caller
     * @param testCaseListener the test case listener
     * @param ma               matching arguments
     * @param ia               filter arguments
     */
    public HeadersIteratorExecutor(ServiceCaller serviceCaller, TestCaseListener testCaseListener, MatchArguments ma, FilterArguments ia) {
        this.serviceCaller = serviceCaller;
        this.testCaseListener = testCaseListener;
        this.matchArguments = ma;
        this.filterArguments = ia;
    }

    /**
     * Executes the actual fuzzing logic.
     *
     * @param context the context used for fuzzing
     */
    public void execute(HeadersIteratorExecutorContext context) {
        Set<DochiaHeader> headersWithoutAuth = this.getHeadersWithoutAuthHeaders(context);
        if (headersWithoutAuth.isEmpty()) {
            context.getLogger().skip("No headers to fuzz");
        }

        Set<DochiaHeader> clonedHeaders = Cloner.cloneMe(headersWithoutAuth);

        for (DochiaHeader header : clonedHeaders) {
            if (filterArguments.getSkipHeaders().stream().noneMatch(ignoredHeader -> ignoredHeader.equalsIgnoreCase(header.getName()))) {
                for (FuzzingStrategy fuzzingStrategy : context.getFuzzValueProducer().get()) {
                    context.getLogger().debug("Fuzzing strategy {} for header {}", fuzzingStrategy.name(), header);
                    String previousHeaderValue = header.getValue();
                    header.withValue(String.valueOf(fuzzingStrategy.process(previousHeaderValue)));
                    try {
                        testCaseListener.createAndExecuteTest(context.getLogger(), context.getTestCasePlaybook(), () -> {
                            boolean isRequiredHeaderFuzzed = clonedHeaders.stream().filter(DochiaHeader::isRequired).toList().contains(header);
                            ResponseCodeFamily expectedResponseCode = this.getExpectedResultCode(isRequiredHeaderFuzzed, context);

                            testCaseListener.addScenario(context.getLogger(), context.getScenario() + "  Current header [{}] [{}]", header.getName(), fuzzingStrategy);
                            testCaseListener.addExpectedResult(context.getLogger(), "Should return [{}]",
                                    expectedResponseCode != null ? expectedResponseCode.asString() : "a response that doesn't match" + matchArguments.getMatchString());

                            ServiceData serviceData = ServiceData.builder()
                                    .relativePath(context.getPlaybookData().getPath())
                                    .contractPath(context.getPlaybookData().getContractPath())
                                    .headers(clonedHeaders)
                                    .payload(context.getPlaybookData().getPayload())
                                    .fuzzedHeader(header.getName())
                                    .queryParams(context.getPlaybookData().getQueryParams())
                                    .httpMethod(context.getPlaybookData().getMethod())
                                    .contentType(context.getPlaybookData().getFirstRequestContentType())
                                    .pathParamsPayload(context.getPlaybookData().getPathParamsPayload())
                                    .build();

                            HttpResponse response = serviceCaller.call(serviceData);
                            this.reportResult(context, expectedResponseCode, response);
                        }, context.getPlaybookData());
                    } finally {
                        /* we reset back the current header */
                        header.withValue(previousHeaderValue);
                    }
                }
            }
        }
    }

    private void reportResult(HeadersIteratorExecutorContext context, ResponseCodeFamily expectedResponseCode, HttpResponse response) {
        if (expectedResponseCode != null) {
            testCaseListener.reportResult(context.getLogger(), context.getPlaybookData(), response, expectedResponseCode, context.isMatchResponseSchema(), context.isShouldMatchContentType());
        } else if (matchArguments.isMatchResponse(response) || !matchArguments.isAnyMatchArgumentSupplied()) {
            testCaseListener.reportResultError(context.getLogger(), context.getPlaybookData(), "Response matches arguments", "Response matches" + matchArguments.getMatchString());
        } else {
            testCaseListener.skipTest(context.getLogger(), "Skipping test as response does not match given matchers!");
        }
    }

    ResponseCodeFamily getExpectedResultCode(boolean required, HeadersIteratorExecutorContext context) {
        return required ? context.getExpectedResponseCodeForRequiredHeaders() : context.getExpectedResponseCodeForOptionalHeaders();
    }

    private Set<DochiaHeader> getHeadersWithoutAuthHeaders(HeadersIteratorExecutorContext context) {
        if (context.isSkipAuthHeaders()) {
            Set<DochiaHeader> headersWithoutAuth = context.getPlaybookData().getHeaders().stream()
                    .filter(dochiaHeader -> !serviceCaller.isAuthenticationHeader(dochiaHeader.getName()))
                    .collect(Collectors.toSet());
            context.getLogger().note("All headers excluding auth headers: {}", headersWithoutAuth);
            return headersWithoutAuth;
        }
        return context.getPlaybookData().getHeaders();
    }
}
