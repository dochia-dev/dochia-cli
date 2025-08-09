package dev.dochia.cli.core.playbook.header.only;

import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class OnlyMultiCodePointEmojisInHeadersPlaybookTest {
    private OnlyMultiCodePointEmojisInHeadersPlaybook onlyMultiCodePointEmojisInHeadersPlaybook;

    @BeforeEach
    void setup() {
        onlyMultiCodePointEmojisInHeadersPlaybook = new OnlyMultiCodePointEmojisInHeadersPlaybook(null);
    }

    @Test
    void shouldHaveReplaceFuzzingStrategy() {
        Assertions.assertThat(onlyMultiCodePointEmojisInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo(FuzzingStrategy.replace().name());
        Assertions.assertThat(onlyMultiCodePointEmojisInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().get(1).getData()).isEqualTo("\uD83D\uDC68\u200D\uD83C\uDFED️");
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(onlyMultiCodePointEmojisInHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldNotMatchResponseSchema() {
        Assertions.assertThat(onlyMultiCodePointEmojisInHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isFalse();
    }

    @Test
    void shouldHaveTypeOfDataToSend() {
        Assertions.assertThat(onlyMultiCodePointEmojisInHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isNotBlank();
    }
}
