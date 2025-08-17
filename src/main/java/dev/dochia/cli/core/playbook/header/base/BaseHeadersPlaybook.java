package dev.dochia.cli.core.playbook.header.base;

import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutorContext;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;

/**
 * Playbook that iterates through all headers and executes fuzzing according to a given {@link BaseHeadersPlaybookContext}.
 */
public abstract class BaseHeadersPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(this.getClass());

    private final HeadersIteratorExecutor headersIteratorExecutor;
    private BaseHeadersPlaybookContext playbookContext;

    /**
     * Creates a new instance of BaseHeadersPlaybook subclass.
     *
     * @param headersIteratorExecutor the executor
     */
    protected BaseHeadersPlaybook(HeadersIteratorExecutor headersIteratorExecutor) {
        this.headersIteratorExecutor = headersIteratorExecutor;
    }

    @Override
    public void run(PlaybookData playbookData) {
        BaseHeadersPlaybookContext context = this.getPlaybookContext();
        headersIteratorExecutor.execute(
                HeadersIteratorExecutorContext.builder()
                        .testCasePlaybook(this)
                        .logger(logger)
                        .expectedResponseCodeForOptionalHeaders(context.getExpectedHttpForOptionalHeadersFuzzed())
                        .expectedResponseCodeForRequiredHeaders(context.getExpectedHttpCodeForRequiredHeadersFuzzed())
                        .fuzzValueProducer(context::getFuzzStrategy)
                        .scenario("Send [%s] in headers.".formatted(context.getTypeOfDataSentToTheService()))
                        .matchResponseSchema(context.isMatchResponseSchema())
                        .shouldMatchContentType(context.isMatchResponseContentType())
                        .playbookData(playbookData)
                        .build());
    }

    /**
     * Retrieves the context for the base headers playbook.
     *
     * <p>This method returns the existing playbook context if it has already been created; otherwise, it creates a new
     * playbook context using the {@code createPlaybookContext} method and caches it for subsequent invocations.</p>
     *
     * @return The BaseHeadersPlaybookContext representing the context for the base headers playbook.
     */
    public BaseHeadersPlaybookContext getPlaybookContext() {
        if (playbookContext == null) {
            playbookContext = createPlaybookContext();
        }
        return playbookContext;
    }

    /**
     * Override this to provide details about Playbook expectations and fuzzing strategy.
     *
     * @return a context to be used to execute the fuzzing
     */
    public abstract BaseHeadersPlaybookContext createPlaybookContext();


    @Override
    public String description() {
        return "Iterate through each header and send %s"
                .formatted(this.getPlaybookContext().getTypeOfDataSentToTheService());
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }
}
