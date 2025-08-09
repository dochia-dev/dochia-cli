package dev.dochia.cli.core.playbook.header.base;

import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Base class for random headers playbooks.
 */
public abstract class BaseRandomHeadersPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(BaseRandomHeadersPlaybook.class);
    private final SimpleExecutor simpleExecutor;
    private final TestCaseListener testCaseListener;

    protected final ProcessingArguments processingArguments;

    /**
     * Constructs a new instance of BaseRandomHeadersPlaybook with protected access.
     *
     * <p>This base playbook is intended for fuzzing scenarios involving random headers.
     * It utilizes a SimpleExecutor for executing fuzzing, relies on a TestCaseListener for handling test case events,
     * and takes ProcessingArguments into account for additional processing.</p>
     *
     * @param simpleExecutor      The SimpleExecutor responsible for executing fuzzing with random headers.
     * @param testCaseListener    The TestCaseListener instance responsible for handling test case events.
     * @param processingArguments The ProcessingArguments containing additional parameters for header fuzzing.
     */
    protected BaseRandomHeadersPlaybook(SimpleExecutor simpleExecutor, TestCaseListener testCaseListener, ProcessingArguments processingArguments) {
        this.simpleExecutor = simpleExecutor;
        this.testCaseListener = testCaseListener;
        this.processingArguments = processingArguments;
    }

    @Override
    public void run(PlaybookData data) {
        List<DochiaHeader> headers = new ArrayList<>(data.getHeaders());

        for (int i = 0; i < processingArguments.getRandomHeadersNumber(); i++) {
            headers.add(DochiaHeader.builder()
                    .name(RandomStringUtils.secure().nextAlphanumeric(10))
                    .required(false)
                    .value(this.randomHeadersValueFunction().apply(10)).build());
        }

        simpleExecutor.execute(
                SimpleExecutorContext.builder()
                        .playbookData(data)
                        .expectedResponseCode(ResponseCodeFamilyPredefined.FOURXX)
                        .testCasePlaybook(this)
                        .logger(logger)
                        .scenario(String.format("Add %s extra random headers.", processingArguments.getRandomHeadersNumber()))
                        .responseProcessor(this::checkResponse)
                        .headers(headers)
                        .build()
        );
    }

    private void checkResponse(HttpResponse response, PlaybookData data) {
        if (ResponseCodeFamilyPredefined.FOURXX.matchesAllowedResponseCodes(String.valueOf(response.getResponseCode()))) {
            testCaseListener.reportResultInfo(logger, data, "Request returned as expected for http method [{}] with response code [{}]",
                    response.getHttpMethod(), response.getResponseCode());
        } else {
            testCaseListener.reportResultError(logger, data, "Unexpected response code: %s".formatted(response.getResponseCode()),
                    "Request failed unexpectedly for http method [{}]: expected {}, actual [{}]", response.getHttpMethod(),
                    ResponseCodeFamilyPredefined.FOURXX.allowedResponseCodes(), response.getResponseCode());
        }
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    /**
     * Override this to provide a concrete implementation for generating headers value.
     *
     * @return the Function generating random header values
     */
    protected abstract Function<Integer, String> randomHeadersValueFunction();
}
