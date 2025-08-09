package dev.dochia.cli.core.playbook.header.leading;

import dev.dochia.cli.core.playbook.api.EmojiPlaybook;
import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybook;
import dev.dochia.cli.core.playbook.header.base.BaseHeadersPlaybookContext;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

/**
 * Playbook that prefixes headers with single code point emojis.
 */
@Singleton
@HeaderPlaybook
@EmojiPlaybook
public class LeadingSingleCodePointEmojisInHeadersPlaybook extends BaseHeadersPlaybook {

    /**
     * Creates a new LeadingSingleCodePointEmojisInHeadersPlaybook instance.
     *
     * @param headersIteratorExecutor the executor
     */
    public LeadingSingleCodePointEmojisInHeadersPlaybook(HeadersIteratorExecutor headersIteratorExecutor) {
        super(headersIteratorExecutor);
    }

    @Override
    public BaseHeadersPlaybookContext createPlaybookContext() {
        return BaseHeadersPlaybookContext.builder()
                .expectedHttpCodeForRequiredHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .expectedHttpForOptionalHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .typeOfDataSentToTheService("values prefixed with single code point emojis")
                .fuzzStrategy(UnicodeGenerator.getSingleCodePointEmojis()
                        .stream().map(value -> FuzzingStrategy.prefix().withData(value)).toList())
                .matchResponseSchema(false)
                .build();
    }
}
