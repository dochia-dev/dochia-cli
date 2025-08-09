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
import org.mockito.Mock;
import org.mockito.Mockito;

@QuarkusTest
class VeryLargeUnicodeStringsInFieldsPlaybookTest
 {
    @Mock
    private ProcessingArguments processingArguments;

    private VeryLargeUnicodeStringsInFieldsPlaybook veryLargeUnicodeStringsInFieldsPlaybook;

    @BeforeEach
    void setup() {
        processingArguments = Mockito.mock(ProcessingArguments.class);
        veryLargeUnicodeStringsInFieldsPlaybook = new VeryLargeUnicodeStringsInFieldsPlaybook(null, null, null, processingArguments);
    }

    @Test
    void shouldOverrideDefaultMethods() {
        Assertions.assertThat(veryLargeUnicodeStringsInFieldsPlaybook.description()).isNotNull();
        Assertions.assertThat(veryLargeUnicodeStringsInFieldsPlaybook.typeOfDataSentToTheService()).isNotNull();
    }

    @Test
    void shouldGetPayloadSize() {
        Mockito.when(processingArguments.getLargeStringsSize()).thenReturn(20000);

        Assertions.assertThat(veryLargeUnicodeStringsInFieldsPlaybook.getFieldFuzzingStrategy(null, null).getFirst().getData().toString()).hasSizeGreaterThan(20000);
    }

    @Test
    void shouldGenerateLessThan500() {
        Mockito.when(processingArguments.getLargeStringsSize()).thenReturn(20);
        Assertions.assertThat(veryLargeUnicodeStringsInFieldsPlaybook.getFieldFuzzingStrategy(null, null).getFirst().getData().toString()).hasSize(20 + "dochia".length());
    }

    @Test
    void shouldOverrideToNotMatchPatterns() {
        Assertions.assertThat(veryLargeUnicodeStringsInFieldsPlaybook.shouldCheckForFuzzedValueMatchingPattern()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"POST,true", "GET,false"})
    void shouldNotMatchResponseContentType(HttpMethod method, boolean expected) {
        PlaybookData data = PlaybookData.builder().method(method).build();
        Assertions.assertThat(veryLargeUnicodeStringsInFieldsPlaybook.shouldMatchContentType(data)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"POST,true", "GET,false"})
    void shouldMatchResponseSchema(HttpMethod method, boolean expected) {
        PlaybookData data = PlaybookData.builder().method(method).build();
        Assertions.assertThat(veryLargeUnicodeStringsInFieldsPlaybook.shouldMatchResponseSchema(data)).isEqualTo(expected);
    }
}