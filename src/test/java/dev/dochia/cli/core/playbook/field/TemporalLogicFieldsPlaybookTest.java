package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.Schema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.Set;

@QuarkusTest
class TemporalLogicFieldsPlaybookTest
 {
    private TemporalLogicFieldsPlaybook temporalLogicFieldsPlaybook;
    private FieldsIteratorExecutor executor;

    @BeforeEach
    void setup() {
        executor = Mockito.mock(FieldsIteratorExecutor.class);
        temporalLogicFieldsPlaybook = new TemporalLogicFieldsPlaybook(executor);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(temporalLogicFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(temporalLogicFieldsPlaybook).hasToString("TemporalLogicFieldsPlaybook");
    }

    @Test
    void shouldNotRunWhenNoTemporalFields() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getReqSchema()).thenReturn(new Schema<String>());
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("someField"));
        temporalLogicFieldsPlaybook.run(data);

        Mockito.verifyNoInteractions(executor);
    }

    @ParameterizedTest
    @CsvSource({"startDate",
            "endDate",
            "expiryDate",
            "dueDate",
            "issueDate"})
    void shouldRunWhenTemporalFields(String fieldName) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Schema<String> schema = new Schema<>();
        schema.setExample("Example");
        Mockito.when(data.getReqSchema()).thenReturn(schema);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of(fieldName));
        temporalLogicFieldsPlaybook.run(data);
        Mockito.verify(executor, Mockito.times(1)).execute(Mockito.any());
    }
}
