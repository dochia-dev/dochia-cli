package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybook;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybookContext;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

import java.util.Collections;

/**
 * Sends zalgo text in headers.
 */
@HeaderPlaybook
@Singleton
public class ZalgoTextInHeadersPlaybook extends BaseHeadersPlaybook {

    /**
     * Creates a new instance.
     *
     * @param headersIteratorExecutor executor used to execute the fuzz logic
     */
    public ZalgoTextInHeadersPlaybook(HeadersIteratorExecutor headersIteratorExecutor) {
        super(headersIteratorExecutor);
    }

    @Override
    public BaseHeadersPlaybookContext createPlaybookContext() {
        return BaseHeadersPlaybookContext.builder()
                .expectedHttpCodeForRequiredHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .expectedHttpForOptionalHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .typeOfDataSentToTheService("zalgo text")
                .fuzzStrategy(Collections.singletonList(FuzzingStrategy.replace().withData(UnicodeGenerator.getZalgoText())))
                .matchResponseSchema(false)
                .build();
    }
}