package dev.dochia.cli.core.playbook.executor;

import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.function.Supplier;

/**
 * Context used by the HeadersIteratorExecutor.
 */
@Builder
@Value
public class HeadersIteratorExecutorContext {
    PrettyLogger logger;

    PlaybookData playbookData;

    String scenario;
    /**
     * If you provide an expected response code, it will be used and matched against what the service returns and report info, warn or error accordingly.
     * If not supplied, FieldsIteratorExecutor will test for Marching Arguments. If any match is found it will be reported as error, otherwise the test will be marked as skipped.
     */
    ResponseCodeFamily expectedResponseCodeForRequiredHeaders;
    /**
     * If you provide an expected response code, it will be used and matched against what the service returns and report info, warn or error accordingly.
     * If not supplied, FieldsIteratorExecutor will test for Marching Arguments. If any match is found it will be reported as error, otherwise the test will be marked as skipped.
     */
    ResponseCodeFamily expectedResponseCodeForOptionalHeaders;
    TestCasePlaybook testCasePlaybook;

    Supplier<List<FuzzingStrategy>> fuzzValueProducer;

    @Builder.Default
    boolean matchResponseSchema = true;

    @Builder.Default
    boolean skipAuthHeaders = true;

    @Builder.Default
    boolean shouldMatchContentType = true;
}
