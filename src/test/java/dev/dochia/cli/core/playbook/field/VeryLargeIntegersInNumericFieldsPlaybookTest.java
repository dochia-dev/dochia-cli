package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.Map;

@QuarkusTest
class VeryLargeIntegersInNumericFieldsPlaybookTest
 {
    private VeryLargeIntegersInNumericFieldsPlaybook veryLargeIntegersInNumericFieldsPlaybook;
    private ProcessingArguments processingArguments;

    @BeforeEach
    void setup() {
        processingArguments = Mockito.mock(ProcessingArguments.class);
        veryLargeIntegersInNumericFieldsPlaybook = new VeryLargeIntegersInNumericFieldsPlaybook(null, null, null, processingArguments);
    }

    @Test
    void shouldReturnDescriptionAndTypeOfData() {
        Assertions.assertThat(veryLargeIntegersInNumericFieldsPlaybook.description()).isNotNull();
        Assertions.assertThat(veryLargeIntegersInNumericFieldsPlaybook.typeOfDataSentToTheService()).isNotNull();

    }

    @Test
    void shouldGetPayloadSizeForNumberSchema() {
        Mockito.when(processingArguments.getLargeStringsSize()).thenReturn(20000);
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("myField", new NumberSchema()));

        Assertions.assertThat(veryLargeIntegersInNumericFieldsPlaybook.getFieldFuzzingStrategy(data, "myField").getFirst().getData().toString()).hasSize(20000);
    }

    @Test
    void shouldGetPayloadSizeForIntegerSchema() {
        Mockito.when(processingArguments.getLargeStringsSize()).thenReturn(30000);
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("myField", new IntegerSchema()));

        Assertions.assertThat(veryLargeIntegersInNumericFieldsPlaybook.getFieldFuzzingStrategy(data, "myField").getFirst().getData().toString()).hasSize(30000);
    }

    @Test
    void shouldSkipWhenNotNumericFields() {
        Mockito.when(processingArguments.getLargeStringsSize()).thenReturn(30000);
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("myField", new StringSchema()));

        Assertions.assertThat(veryLargeIntegersInNumericFieldsPlaybook.getFieldFuzzingStrategy(data, "myField").getFirst().isSkip()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"POST,true", "GET,false"})
    void shouldNotMatchResponseContentType(HttpMethod method, boolean expected) {
        PlaybookData data = PlaybookData.builder().method(method).build();
        Assertions.assertThat(veryLargeIntegersInNumericFieldsPlaybook.shouldMatchContentType(data)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"POST,true", "GET,false"})
    void shouldMatchResponseSchema(HttpMethod method, boolean expected) {
        PlaybookData data = PlaybookData.builder().method(method).build();
        Assertions.assertThat(veryLargeIntegersInNumericFieldsPlaybook.shouldMatchResponseSchema(data)).isEqualTo(expected);
    }
}
