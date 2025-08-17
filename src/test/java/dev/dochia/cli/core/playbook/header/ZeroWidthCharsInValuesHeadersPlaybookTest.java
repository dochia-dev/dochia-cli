package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class ZeroWidthCharsInValuesHeadersPlaybookTest {
    private ZeroWidthCharsInValuesHeadersPlaybook zeroWidthCharsInValuesHeadersPlaybook;

    @BeforeEach
    void setup() {
        zeroWidthCharsInValuesHeadersPlaybook = new ZeroWidthCharsInValuesHeadersPlaybook(Mockito.mock(HeadersIteratorExecutor.class));
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(zeroWidthCharsInValuesHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldNotMatchResponseSchema() {
        Assertions.assertThat(zeroWidthCharsInValuesHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isFalse();
    }

    @Test
    void shouldHaveInsertFuzzingStrategy() {
        Assertions.assertThat(zeroWidthCharsInValuesHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo("INSERT");
    }

    @Test
    void shouldReturnZeroWidthChars() {
        Assertions.assertThat(zeroWidthCharsInValuesHeadersPlaybook.getPlaybookContext().getFuzzStrategy()).hasSize(11);
        Assertions.assertThat(zeroWidthCharsInValuesHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().getData()).hasToString("\u200b");
    }

    @Test
    void shouldReturnTypeOfDataToSend() {
        Assertions.assertThat(zeroWidthCharsInValuesHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isEqualTo("zero-width characters");
    }
}
