package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybook;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybookContext;
import dev.dochia.cli.core.generator.simple.StringGenerator;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

import java.util.Collections;


/**
 * Playbook sending very large strings in headers.
 */
@Singleton
@HeaderPlaybook
public class VeryLargeStringsInHeadersPlaybook extends BaseHeadersPlaybook {
    private final ProcessingArguments processingArguments;

    /**
     * Creates a new instance.
     *
     * @param headersIteratorExecutor executor used to run the fuzz logic
     * @param pa                      used to get the size of the strings
     */
    public VeryLargeStringsInHeadersPlaybook(HeadersIteratorExecutor headersIteratorExecutor, ProcessingArguments pa) {
        super(headersIteratorExecutor);
        this.processingArguments = pa;
    }

    @Override
    public BaseHeadersPlaybookContext createPlaybookContext() {
        return BaseHeadersPlaybookContext.builder()
                .expectedHttpCodeForRequiredHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .expectedHttpForOptionalHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .typeOfDataSentToTheService("large values")
                .fuzzStrategy(Collections.singletonList(
                        FuzzingStrategy.replace().withData(
                                StringGenerator.generateLargeString(processingArguments.getLargeStringsSize() / 4))))
                .matchResponseSchema(false)
                .matchResponseContentType(false)
                .build();
    }
}
