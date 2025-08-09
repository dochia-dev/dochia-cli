package dev.dochia.cli.core.playbook.header.only;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybook;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybookContext;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

/**
 * Playbook that sends only spaces in headers.
 */
@Singleton
@HeaderPlaybook
public class OnlySpacesInHeadersPlaybook extends BaseHeadersPlaybook {

    /**
     * Creates a new OnlySpacesInHeadersPlaybook instance.
     *
     * @param headersIteratorExecutor the executor
     */
    protected OnlySpacesInHeadersPlaybook(HeadersIteratorExecutor headersIteratorExecutor) {
        super(headersIteratorExecutor);
    }

    @Override
    public BaseHeadersPlaybookContext createPlaybookContext() {
        return BaseHeadersPlaybookContext.builder()
                .expectedHttpCodeForRequiredHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .expectedHttpForOptionalHeadersFuzzed(ResponseCodeFamilyPredefined.TWOXX)
                .typeOfDataSentToTheService("values replaced by spaces")
                .fuzzStrategy(UnicodeGenerator.getSpacesHeaders()
                        .stream().map(value -> FuzzingStrategy.replace().withData(value)).toList())
                .build();
    }

}
