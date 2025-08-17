package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybook;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybookContext;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

/**
 * Fuzzes HTTP headers by injecting zero-width characters. This has a limited set of characters that are used.
 */
@HeaderPlaybook
@Singleton
public class ZeroWidthCharsInValuesHeadersPlaybook extends BaseHeadersPlaybook {

    /**
     * Creates a new instance.
     *
     * @param headersIteratorExecutor executor used to run the fuzzing logic
     */
    protected ZeroWidthCharsInValuesHeadersPlaybook(HeadersIteratorExecutor headersIteratorExecutor) {
        super(headersIteratorExecutor);
    }

    @Override
    public BaseHeadersPlaybookContext createPlaybookContext() {
        return BaseHeadersPlaybookContext.builder()
                .expectedHttpCodeForRequiredHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .expectedHttpForOptionalHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .typeOfDataSentToTheService("zero-width characters")
                .fuzzStrategy(UnicodeGenerator.getZwCharsSmallListHeaders().stream().map(value -> FuzzingStrategy.insert().withData(value)).toList())
                .matchResponseSchema(false)
                .build();
    }
}
