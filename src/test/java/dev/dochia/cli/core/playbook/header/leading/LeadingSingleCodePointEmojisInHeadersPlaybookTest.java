package dev.dochia.cli.core.playbook.header.leading;

import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class LeadingSingleCodePointEmojisInHeadersPlaybookTest {
    private LeadingSingleCodePointEmojisInHeadersPlaybook leadingSingleCodePointEmojisInHeadersPlaybook;

    @BeforeEach
    void setup() {
        leadingSingleCodePointEmojisInHeadersPlaybook = new LeadingSingleCodePointEmojisInHeadersPlaybook(null);
    }

    @Test
    void shouldNotMatchResponseSchema() {
        Assertions.assertThat(leadingSingleCodePointEmojisInHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isFalse();
    }

    @Test
    void shouldReturnPrefixFuzzingStrategy() {
        Assertions.assertThat(leadingSingleCodePointEmojisInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo(FuzzingStrategy.prefix().name());
        Assertions.assertThat(leadingSingleCodePointEmojisInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().get(1).getData()).isEqualTo("\uD83D\uDC80");
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(leadingSingleCodePointEmojisInHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveTypeOfDataToSend() {
        Assertions.assertThat(leadingSingleCodePointEmojisInHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isNotBlank();
    }
}
