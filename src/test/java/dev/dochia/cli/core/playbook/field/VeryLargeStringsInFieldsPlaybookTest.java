package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

@QuarkusTest
class VeryLargeStringsInFieldsPlaybookTest
 {
    private VeryLargeStringsInFieldsPlaybook veryLargeStringsInFieldsPlaybook;
    private ProcessingArguments processingArguments;

    @BeforeEach
    void setup() {
        processingArguments = Mockito.mock(ProcessingArguments.class);
        veryLargeStringsInFieldsPlaybook = new VeryLargeStringsInFieldsPlaybook(null, null, null, processingArguments);
    }

    @Test
    void givenANewVeryLargeStringsPlaybook_whenCreatingANewInstance_thenTheMethodsBeingOverriddenAreMatchingTheVeryLargeStringsPlaybook() {
        Assertions.assertThat(veryLargeStringsInFieldsPlaybook.description()).isNotNull();
        Assertions.assertThat(veryLargeStringsInFieldsPlaybook.typeOfDataSentToTheService()).isNotNull();

    }

    @Test
    void shouldGetPayloadSize() {
        Mockito.when(processingArguments.getLargeStringsSize()).thenReturn(20000);

        Assertions.assertThat(veryLargeStringsInFieldsPlaybook.getFieldFuzzingStrategy(null, null).getFirst().getData().toString()).hasSize(20000);
    }

    @Test
    void shouldOverrideToNotMatchPatterns() {
        Assertions.assertThat(veryLargeStringsInFieldsPlaybook.shouldCheckForFuzzedValueMatchingPattern()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"POST,true", "GET,false"})
    void shouldNotMatchResponseContentType(HttpMethod method, boolean expected) {
        PlaybookData data = PlaybookData.builder().method(method).build();
        Assertions.assertThat(veryLargeStringsInFieldsPlaybook.shouldMatchContentType(data)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"POST,true", "GET,false"})
    void shouldMatchResponseSchema(HttpMethod method, boolean expected) {
        PlaybookData data = PlaybookData.builder().method(method).build();
        Assertions.assertThat(veryLargeStringsInFieldsPlaybook.shouldMatchResponseSchema(data)).isEqualTo(expected);
    }
}
