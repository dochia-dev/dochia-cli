package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybook;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybookContext;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;

/**
 * Sends empty string in all HTTP headers declared in the OpenAPI spec.
 */
@Singleton
@HeaderPlaybook
public class EmptyStringsInHeadersPlaybook extends BaseHeadersPlaybook {

    /**
     * Creates a new instance.
     *
     * @param headersIteratorExecutor executor used to run the fuzz logic
     */
    public EmptyStringsInHeadersPlaybook(HeadersIteratorExecutor headersIteratorExecutor) {
        super(headersIteratorExecutor);
    }

    @Override
    public BaseHeadersPlaybookContext createPlaybookContext() {
        return BaseHeadersPlaybookContext.builder()
                .expectedHttpCodeForRequiredHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .expectedHttpForOptionalHeadersProducer(header -> {
                    boolean shouldBe4xx = !header.isRequired() && StringUtils.isNotBlank(header.getFormat());
                    return shouldBe4xx ? ResponseCodeFamilyPredefined.FOURXX_TWOXX : ResponseCodeFamilyPredefined.TWOXX;
                })                .typeOfDataSentToTheService("empty values")
                .fuzzStrategy(Collections.singletonList(FuzzingStrategy.replace().withData("")))
                .build();
    }

}
