package dev.dochia.cli.core.playbook.field;


import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.Schema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

@QuarkusTest
class ExamplesFieldsPlaybookTest
 {

    private ExamplesFieldsPlaybook examplesFieldsPlaybook;
    private SimpleExecutor simpleExecutor;

    @BeforeEach
    void setup() {
        simpleExecutor = Mockito.mock(SimpleExecutor.class);

        examplesFieldsPlaybook = new ExamplesFieldsPlaybook(simpleExecutor);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(examplesFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(examplesFieldsPlaybook).hasToString("ExamplesFieldsPlaybook");
    }

    @Test
    void shouldNotRunWhenNoExamples() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getReqSchema()).thenReturn(new Schema<String>());
        examplesFieldsPlaybook.run(data);

        Mockito.verifyNoInteractions(simpleExecutor);
    }

    @Test
    void shouldExecuteBasedOnExample() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Schema<String> schema = new Schema<>();
        schema.setExample("Example");
        Mockito.when(data.getReqSchema()).thenReturn(schema);

        examplesFieldsPlaybook.run(data);
        Mockito.verify(simpleExecutor, Mockito.times(1)).execute(Mockito.any());
    }

    @Test
    void shouldExecuteBasedOnExamples() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Schema<String> schema = new Schema<>();
        schema.setExamples(List.of("Example1", "Example2"));
        Mockito.when(data.getReqSchema()).thenReturn(schema);

        examplesFieldsPlaybook.run(data);
        Mockito.verify(simpleExecutor, Mockito.times(2)).execute(Mockito.any());
    }
}
