package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.util.DochiaRandom;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

@QuarkusTest
class InsertWhitespacesInFieldNamesFieldPlaybookTest {
    private SimpleExecutor simpleExecutor;

    private InsertWhitespacesInFieldNamesFieldPlaybook insertWhitespacesInFieldNamesFieldPlaybook;

    @BeforeEach
    void setup() {
        DochiaRandom.initRandom(0);
        simpleExecutor = Mockito.mock(SimpleExecutor.class);
        insertWhitespacesInFieldNamesFieldPlaybook = new InsertWhitespacesInFieldNamesFieldPlaybook(simpleExecutor);
    }

    @Test
    void shouldNotRunForEmptyPayload() {
        insertWhitespacesInFieldNamesFieldPlaybook.run(Mockito.mock(PlaybookData.class));

        Mockito.verifyNoInteractions(simpleExecutor);
    }

    @Test
    void shouldNotRunForFieldNotInPayload() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getPayload()).thenReturn("{\"field1\": \"value1\"}");
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("field2"));
        insertWhitespacesInFieldNamesFieldPlaybook.run(data);

        Mockito.verifyNoInteractions(simpleExecutor);
    }

    @Test
    void shouldRunWhenFieldInPayload() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getPayload()).thenReturn("{\"field1\": \"value1\"}");
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("field1"));
        insertWhitespacesInFieldNamesFieldPlaybook.run(data);

        Mockito.verify(simpleExecutor).execute(Mockito.any());
    }

    @Test
    void shouldHaveProperDescriptionAndToString() {
        Assertions.assertThat(insertWhitespacesInFieldNamesFieldPlaybook.description()).isNotNull();
        Assertions.assertThat(insertWhitespacesInFieldNamesFieldPlaybook).hasToString(insertWhitespacesInFieldNamesFieldPlaybook.getClass().getSimpleName());
    }
}
