package dev.dochia.cli.core.playbook.header.trailing;

import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TrailingSpacesInHeadersPlaybookTest {
    private TrailingSpacesInHeadersPlaybook trailingSpacesInHeadersPlaybook;

    @BeforeEach
    void setup() {
        trailingSpacesInHeadersPlaybook = new TrailingSpacesInHeadersPlaybook(null);
    }

    @Test
    void shouldReturnTrailFuzzingStrategy() {
        Assertions.assertThat(trailingSpacesInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo(FuzzingStrategy.trail().name());
        Assertions.assertThat(trailingSpacesInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().getData()).isEqualTo(" ");
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(trailingSpacesInHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldMatchResponseSchema() {
        Assertions.assertThat(trailingSpacesInHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isTrue();
    }

    @Test
    void shouldHaveTypeOfDataToSend() {
        Assertions.assertThat(trailingSpacesInHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isNotBlank();
    }
}
