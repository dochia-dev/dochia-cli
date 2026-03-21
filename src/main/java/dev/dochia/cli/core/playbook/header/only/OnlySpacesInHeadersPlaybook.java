package dev.dochia.cli.core.playbook.header.only;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybook;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybookContext;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

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
                .expectedHttpForOptionalHeadersProducer(header -> {
                    boolean shouldBe4xx = !header.isRequired() && StringUtils.isNotBlank(header.getFormat());
                    return shouldBe4xx ? ResponseCodeFamilyPredefined.FOURXX_TWOXX : ResponseCodeFamilyPredefined.TWOXX;
                })
                .typeOfDataSentToTheService("values replaced by spaces")
                .fuzzStrategy(UnicodeGenerator.getSpacesHeaders()
                        .stream().map(value -> FuzzingStrategy.replace().withData(value)).toList())
                .build();
    }

}
