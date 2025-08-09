package dev.dochia.cli.core.playbook.header.leading;

import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class LeadingMultiCodePointEmojisInHeadersPlaybookTest {
    private LeadingMultiCodePointEmojisInHeadersPlaybook leadingMultiCodePointEmojisInHeadersPlaybook;

    @BeforeEach
    void setup() {
        leadingMultiCodePointEmojisInHeadersPlaybook = new LeadingMultiCodePointEmojisInHeadersPlaybook(null);
    }

    @Test
    void shouldNotMatchResponseSchema() {
        Assertions.assertThat(leadingMultiCodePointEmojisInHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isFalse();
    }

    @Test
    void shouldReturnPrefixFuzzingStrategy() {
        Assertions.assertThat(leadingMultiCodePointEmojisInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().get(0).name()).isEqualTo(FuzzingStrategy.prefix().name());
        Assertions.assertThat(leadingMultiCodePointEmojisInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().get(1).getData()).isEqualTo("\uD83D\uDC68\u200D\uD83C\uDFED️");
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(leadingMultiCodePointEmojisInHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveTypeOfDataToSend() {
        Assertions.assertThat(leadingMultiCodePointEmojisInHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isNotBlank();
    }
}
