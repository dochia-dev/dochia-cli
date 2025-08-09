package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

@QuarkusTest
class ZeroWidthCharsInNamesHeadersPlaybookTest {
    private ZeroWidthCharsInNamesHeadersPlaybook zeroWidthCharsInNamesHeadersPlaybook;
    private SimpleExecutor simpleExecutor;

    @BeforeEach
    void setup() {
        simpleExecutor = Mockito.mock(SimpleExecutor.class);
        zeroWidthCharsInNamesHeadersPlaybook = new ZeroWidthCharsInNamesHeadersPlaybook(simpleExecutor);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(zeroWidthCharsInNamesHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(zeroWidthCharsInNamesHeadersPlaybook).hasToString(zeroWidthCharsInNamesHeadersPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldNotRunSimpleExecutorWhenNoHeaders() {
        zeroWidthCharsInNamesHeadersPlaybook.run(PlaybookData.builder().headers(Set.of()).build());
        Mockito.verifyNoInteractions(simpleExecutor);
    }

    @Test
    void shouldRunWhenHeadersArePresent() {
        zeroWidthCharsInNamesHeadersPlaybook.run(PlaybookData.builder().headers(Set.of(DochiaHeader.builder().name("test").value("value").build())).build());
        Mockito.verify(simpleExecutor, Mockito.times(11)).execute(Mockito.any());
    }
}
