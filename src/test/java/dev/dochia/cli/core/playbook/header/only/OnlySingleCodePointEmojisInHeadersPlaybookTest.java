package dev.dochia.cli.core.playbook.header.only;

import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class OnlySingleCodePointEmojisInHeadersPlaybookTest {
    private OnlySingleCodePointEmojisInHeadersPlaybook onlySingleCodePointEmojisInHeadersPlaybook;

    @BeforeEach
    void setup() {
        onlySingleCodePointEmojisInHeadersPlaybook = new OnlySingleCodePointEmojisInHeadersPlaybook(null);
    }

    @Test
    void shouldHaveReplaceFuzzingStrategy() {
        Assertions.assertThat(onlySingleCodePointEmojisInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo(FuzzingStrategy.replace().name());
        Assertions.assertThat(onlySingleCodePointEmojisInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().get(1).getData()).isEqualTo("\uD83D\uDC80");
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(onlySingleCodePointEmojisInHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldNotMatchResponseSchema() {
        Assertions.assertThat(onlySingleCodePointEmojisInHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isFalse();
    }

    @Test
    void shouldHaveTypeOfDataToSend() {
        Assertions.assertThat(onlySingleCodePointEmojisInHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isNotBlank();
    }
}
