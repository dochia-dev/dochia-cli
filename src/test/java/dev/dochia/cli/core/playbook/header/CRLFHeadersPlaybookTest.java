package dev.dochia.cli.core.playbook.header;


import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class CRLFHeadersPlaybookTest
 {
    private CRLFHeadersPlaybook crlfHeadersPlaybook;

    @BeforeEach
    void setup() {
        crlfHeadersPlaybook = new CRLFHeadersPlaybook(Mockito.mock(HeadersIteratorExecutor.class));
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(crlfHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldNotMatchResponseSchema() {
        Assertions.assertThat(crlfHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isFalse();
    }

    @Test
    void shouldHaveReplaceFuzzingStrategy() {
        Assertions.assertThat(crlfHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo("REPLACE");
    }

    @Test
    void shouldReturnCrLfInvisibleChars() {
        Assertions.assertThat(crlfHeadersPlaybook.getPlaybookContext().getFuzzStrategy()).hasSize(1);
        Assertions.assertThat(crlfHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().getData()).hasToString("\r\n");
    }

    @Test
    void shouldReturnTypeOfDataToSend() {
        Assertions.assertThat(crlfHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isEqualTo("CR & LF characters");
    }

}
