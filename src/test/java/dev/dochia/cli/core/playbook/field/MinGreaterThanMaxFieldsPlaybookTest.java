package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
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
class MinGreaterThanMaxFieldsPlaybookTest
 {
    private MinGreaterThanMaxFieldsPlaybook minGreaterThanMaxFieldsPlaybook;
    private SimpleExecutor executor;

    @BeforeEach
    void setup() {
        executor = Mockito.mock(SimpleExecutor.class);
        minGreaterThanMaxFieldsPlaybook = new MinGreaterThanMaxFieldsPlaybook(executor);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(minGreaterThanMaxFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(minGreaterThanMaxFieldsPlaybook).hasToString("MinGreaterThanMaxFieldsPlaybook");
    }

    @Test
    void shouldNotRunWhenNoMinMaxFields() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getReqSchema()).thenReturn(new Schema<String>());
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("someField"));
        minGreaterThanMaxFieldsPlaybook.run(data);

        Mockito.verifyNoInteractions(executor);
    }

    @ParameterizedTest
    @CsvSource({"minPrice,someField", "maxPrice,someField", "minPrice,maxValue"})
    void shouldNotRunWhenOneBoundaryField(String field1, String field2) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Schema<String> schema = new Schema<>();
        schema.setExample("Example");
        Mockito.when(data.getReqSchema()).thenReturn(schema);
        Mockito.when(data.getPayload()).thenReturn("""
                    {
                        "minPrice": 50,
                        "maxPrice": 100,
                        "maxValue": 200,
                        "someField": 0
                    }
                """);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of(field1, field2));
        minGreaterThanMaxFieldsPlaybook.run(data);

        Mockito.verifyNoInteractions(executor);
    }

    @Test
    void shouldNotRunWhenNamingMatchesButNotNumbers() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Schema<String> schema = new Schema<>();
        schema.setExample("Example");
        Mockito.when(data.getReqSchema()).thenReturn(schema);
        Mockito.when(data.getPayload()).thenReturn("""
                    {
                        "minPrice": "50",
                        "maxPrice": 100,
                        "maxValue": "200",
                        "minValue": "100
                    }
                """);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("minPrice", "maxPrice", "minValue", "maxValue"));
        minGreaterThanMaxFieldsPlaybook.run(data);

        Mockito.verifyNoInteractions(executor);
    }

    @Test
    void shouldRunWhenMinMaxFieldsAtStart() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Schema<String> schema = new Schema<>();
        schema.setExample("Example");
        Mockito.when(data.getReqSchema()).thenReturn(schema);
        Mockito.when(data.getPayload()).thenReturn("""
                    {
                        "minPrice": 50,
                        "maxPrice": 100
                    }
                """);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("minPrice", "maxPrice"));
        minGreaterThanMaxFieldsPlaybook.run(data);

        Mockito.verify(executor, Mockito.times(1)).execute(Mockito.any());
    }

    @Test
    void shouldRunWhenMinMaxFieldsAtEnd() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Schema<String> schema = new Schema<>();
        schema.setExample("Example");
        Mockito.when(data.getReqSchema()).thenReturn(schema);
        Mockito.when(data.getPayload()).thenReturn("""
                    {
                        "priceMin": 50,
                        "priceMax": 100
                    }
                """);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("priceMin", "priceMax"));
        minGreaterThanMaxFieldsPlaybook.run(data);

        Mockito.verify(executor, Mockito.times(1)).execute(Mockito.any());
    }
}
