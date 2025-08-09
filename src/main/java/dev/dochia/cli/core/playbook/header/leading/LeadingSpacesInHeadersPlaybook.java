package dev.dochia.cli.core.playbook.header.leading;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybook;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybookContext;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Playbook that prefixes headers with spaces.
 */
@Singleton
@HeaderPlaybook
public class LeadingSpacesInHeadersPlaybook extends BaseHeadersPlaybook {

    /**
     * Creates a new LeadingSpacesInHeadersPlaybook instance.
     *
     * @param headersIteratorExecutor the executor
     */
    protected LeadingSpacesInHeadersPlaybook(HeadersIteratorExecutor headersIteratorExecutor) {
        super(headersIteratorExecutor);
    }

    @Override
    public BaseHeadersPlaybookContext createPlaybookContext() {
        return BaseHeadersPlaybookContext.builder()
                .expectedHttpCodeForRequiredHeadersFuzzed(ResponseCodeFamilyPredefined.TWOXX)
                .expectedHttpForOptionalHeadersFuzzed(ResponseCodeFamilyPredefined.TWOXX)
                .typeOfDataSentToTheService("values prefixed with spaces")
                .fuzzStrategy(getInvisibleChars()
                        .stream().map(value -> FuzzingStrategy.prefix().withData(value)).toList())
                .build();
    }

    /**
     * Returns a list of invisible chars to be used for fuzzing.
     *
     * @return a list of values to be used for fuzzing
     */
    public List<String> getInvisibleChars() {
        List<String> leadingChars = new ArrayList<>(UnicodeGenerator.getSpacesHeaders());
        leadingChars.remove("\r");
        return leadingChars;
    }
}
