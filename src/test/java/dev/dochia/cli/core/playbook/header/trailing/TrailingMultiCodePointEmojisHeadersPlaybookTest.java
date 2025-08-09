package dev.dochia.cli.core.playbook.header.trailing;

import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TrailingMultiCodePointEmojisHeadersPlaybookTest {
    private TrailingMultiCodePointEmojisHeadersPlaybook trailingMultiCodePointEmojisHeadersPlaybook;

    @BeforeEach
    void setup() {
        trailingMultiCodePointEmojisHeadersPlaybook = new TrailingMultiCodePointEmojisHeadersPlaybook(null);
    }

    @Test
    void shouldReturnTrailFuzzingStrategy() {
        Assertions.assertThat(trailingMultiCodePointEmojisHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo(FuzzingStrategy.trail().name());
        Assertions.assertThat(trailingMultiCodePointEmojisHeadersPlaybook.getPlaybookContext().getFuzzStrategy().get(1).getData()).isEqualTo("\uD83D\uDC68\u200D\uD83C\uDFED️");
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(trailingMultiCodePointEmojisHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldNotMatchResponseSchema() {
        Assertions.assertThat(trailingMultiCodePointEmojisHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isFalse();
    }

    @Test
    void shouldHaveTypeOfDataToSend() {
        Assertions.assertThat(trailingMultiCodePointEmojisHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isNotBlank();
    }
}
