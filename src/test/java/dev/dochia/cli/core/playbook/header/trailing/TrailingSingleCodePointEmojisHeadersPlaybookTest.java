package dev.dochia.cli.core.playbook.header.trailing;

import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TrailingSingleCodePointEmojisHeadersPlaybookTest {
    private TrailingSingleCodePointEmojisHeadersPlaybook trailingSingleCodePointEmojisHeadersPlaybook;

    @BeforeEach
    void setup() {
        trailingSingleCodePointEmojisHeadersPlaybook = new TrailingSingleCodePointEmojisHeadersPlaybook(null);
    }

    @Test
    void shouldReturnTrailFuzzingStrategy() {
        Assertions.assertThat(trailingSingleCodePointEmojisHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo(FuzzingStrategy.trail().name());
        Assertions.assertThat(trailingSingleCodePointEmojisHeadersPlaybook.getPlaybookContext().getFuzzStrategy().get(1).getData()).isEqualTo("\uD83D\uDC80");
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(trailingSingleCodePointEmojisHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldNotMatchResponseSchema() {
        Assertions.assertThat(trailingSingleCodePointEmojisHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isFalse();
    }

    @Test
    void shouldHaveTypeOfDataToSend() {
        Assertions.assertThat(trailingSingleCodePointEmojisHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isNotBlank();
    }
}
