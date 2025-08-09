package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Playbook that checks if response headers match the ones defined in the contract.
 */
@Singleton
@HeaderPlaybook
public class ResponseHeadersMatchContractHeadersPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(this.getClass());
    private final SimpleExecutor simpleExecutor;
    private final TestCaseListener testCaseListener;

    /**
     * Constructs a new instance of ResponseHeadersMatchContractHeadersPlaybook.
     *
     * <p>This playbook is designed to match response headers against contract headers and utilizes a SimpleExecutor
     * for executing fuzzing. The provided TestCaseListener is used for handling test case events.</p>
     *
     * @param testCaseListener The TestCaseListener instance responsible for handling test case events.
     * @param simpleExecutor   The SimpleExecutor responsible for executing fuzzing with response headers matching contract headers.
     */
    @Inject
    public ResponseHeadersMatchContractHeadersPlaybook(TestCaseListener testCaseListener, SimpleExecutor simpleExecutor) {
        this.simpleExecutor = simpleExecutor;
        this.testCaseListener = testCaseListener;
    }

    @Override
    public void run(PlaybookData data) {
        if (!data.getResponseHeaders().isEmpty()) {
            simpleExecutor.execute(SimpleExecutorContext.builder()
                    .playbookData(data)
                    .expectedResponseCode(ResponseCodeFamilyPredefined.TWOXX)
                    .testCasePlaybook(this)
                    .payload(data.getPayload())
                    .scenario("Send a 'happy' flow request with all fields and all headers and checks if the response headers match those declared in the contract")
                    .logger(logger)
                    .responseProcessor(this::processResponse)
                    .build());
        }
    }

    private void processResponse(HttpResponse httpResponse, PlaybookData playbookData) {
        Set<String> expectedResponseHeaders = Optional.ofNullable(playbookData.getResponseHeaders().get(httpResponse.responseCodeAsString()))
                .orElse(Collections.emptySet());

        Set<String> notReturnedHeaders = expectedResponseHeaders.stream()
                .filter(name -> !httpResponse.containsHeader(name))
                .collect(Collectors.toCollection(TreeSet::new));

        if (notReturnedHeaders.isEmpty()) {
            testCaseListener.reportResult(logger, playbookData, httpResponse, ResponseCodeFamilyPredefined.TWOXX);
        } else {
            testCaseListener.reportResultError(logger, playbookData, "Missing response headers",
                    "The following response headers defined in the contract are missing: {}", notReturnedHeaders.toArray());
        }
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public String description() {
        return "send a request with all fields and headers populated and checks if the response headers match the ones defined in the contract";
    }
}
