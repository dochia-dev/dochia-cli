package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybook;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybookContext;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

/**
 * Sends very large unicode strings into headers.
 */
@Singleton
@HeaderPlaybook
public class VeryLargeUnicodeStringsInHeadersPlaybook extends BaseHeadersPlaybook {
    private final ProcessingArguments processingArguments;

    /**
     * Creates a new instance.
     *
     * @param headersIteratorExecutor the executor used to run the fuzz logic
     * @param pa                      used to get the size of the strings
     */
    public VeryLargeUnicodeStringsInHeadersPlaybook(HeadersIteratorExecutor headersIteratorExecutor, ProcessingArguments pa) {
        super(headersIteratorExecutor);
        this.processingArguments = pa;
    }

    @Override
    public BaseHeadersPlaybookContext createPlaybookContext() {
        return BaseHeadersPlaybookContext.builder()
                .expectedHttpCodeForRequiredHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .expectedHttpForOptionalHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .typeOfDataSentToTheService("large unicode values")
                .fuzzStrategy(FuzzingStrategy.getLargeValuesStrategy(processingArguments.getLargeStringsSize()))
                .matchResponseSchema(false)
                .matchResponseContentType(false)
                .build();
    }

}