package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class EmptyStringsInHeadersPlaybookTest {
    private EmptyStringsInHeadersPlaybook emptyStringsInHeadersPlaybook;

    @BeforeEach
    void setup() {
        emptyStringsInHeadersPlaybook = new EmptyStringsInHeadersPlaybook(null);
    }

    @Test
    void shouldReturnReplaceFuzzingStrategy() {
        Assertions.assertThat(emptyStringsInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo(FuzzingStrategy.replace().name());
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(emptyStringsInHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldMatchResponseSchema() {
        Assertions.assertThat(emptyStringsInHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isTrue();
    }

    @Test
    void shouldHaveTypeOfDataToSend() {
        Assertions.assertThat(emptyStringsInHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isNotBlank();
    }
}
