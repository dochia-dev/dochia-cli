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
 * Playbook that prefixes headers with multi code point emojis.
 */
@Singleton
@HeaderPlaybook
@EmojiPlaybook
public class LeadingMultiCodePointEmojisInHeadersPlaybook extends BaseHeadersPlaybook {

    /**
     * Creates a LeadingMultiCodePointEmojisInHeadersPlaybook instance.
     *
     * @param headersIteratorExecutor the executor
     */
    public LeadingMultiCodePointEmojisInHeadersPlaybook(HeadersIteratorExecutor headersIteratorExecutor) {
        super(headersIteratorExecutor);
    }

    @Override
    public BaseHeadersPlaybookContext createPlaybookContext() {
        return BaseHeadersPlaybookContext.builder()
                .expectedHttpCodeForRequiredHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .expectedHttpForOptionalHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                .typeOfDataSentToTheService("values prefixed with multi code point emojis")
                .fuzzStrategy(UnicodeGenerator.getMultiCodePointEmojis()
                        .stream().map(value -> FuzzingStrategy.prefix().withData(value)).toList())
                .matchResponseSchema(false)
                .build();
    }
}
