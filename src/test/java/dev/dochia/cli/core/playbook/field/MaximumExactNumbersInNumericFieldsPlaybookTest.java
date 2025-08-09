package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

@QuarkusTest
class MaximumExactNumbersInNumericFieldsPlaybookTest
 {
    private MaximumExactNumbersInNumericFieldsPlaybook maximumExactNumbersInNumericFieldsPlaybook;

    private FilesArguments filesArguments;

    @BeforeEach
    void setup() {
        filesArguments = Mockito.mock(FilesArguments.class);
        maximumExactNumbersInNumericFieldsPlaybook = new MaximumExactNumbersInNumericFieldsPlaybook(null, null, filesArguments);
        Mockito.when(filesArguments.getRefData(Mockito.anyString())).thenReturn(Collections.emptyMap());
    }

    @Test
    void shouldApply() {
        Assertions.assertThat(maximumExactNumbersInNumericFieldsPlaybook.getSchemaTypesThePlaybookWillApplyTo()).containsOnly("integer", "number");
    }

    @Test
    void shouldNotRunWhenRefData() {
        Mockito.when(filesArguments.getRefData("/test")).thenReturn(Map.of("test", "value"));
        StringSchema stringSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().path("/test").requestPropertyTypes(Collections.singletonMap("test", stringSchema)).build();
        stringSchema.setMaximum(new BigDecimal(100));
        Assertions.assertThat(maximumExactNumbersInNumericFieldsPlaybook.hasBoundaryDefined("test", data)).isFalse();
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(maximumExactNumbersInNumericFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldNotHaveBoundaryDefined() {
        StringSchema stringSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", stringSchema)).build();
        Assertions.assertThat(maximumExactNumbersInNumericFieldsPlaybook.hasBoundaryDefined("test", data)).isFalse();
    }

    @Test
    void shouldHaveBoundaryDefined() {
        StringSchema stringSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", stringSchema)).build();
        stringSchema.setMaximum(BigDecimal.TEN);
        Assertions.assertThat(maximumExactNumbersInNumericFieldsPlaybook.hasBoundaryDefined("test", data)).isTrue();
    }

    @Test
    void shouldSkipForDeleteAndGet() {
        Assertions.assertThat(maximumExactNumbersInNumericFieldsPlaybook.skipForHttpMethods()).containsOnly(HttpMethod.GET, HttpMethod.DELETE);
    }

    @Test
    void shouldReturnDataToSendToTheService() {
        Assertions.assertThat(maximumExactNumbersInNumericFieldsPlaybook.typeOfDataSentToTheService()).isEqualTo("exact maximum size values");
    }

    @Test
    void shouldHaveBoundaryValue() {
        StringSchema stringSchema = new StringSchema();
        stringSchema.setMaximum(BigDecimal.TEN);
        Assertions.assertThat(maximumExactNumbersInNumericFieldsPlaybook.getBoundaryValue(stringSchema)).isNotNull();
    }

    @Test
    void shouldReturn2XXForExpectedResultCodes() {
        Assertions.assertThat(maximumExactNumbersInNumericFieldsPlaybook.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(maximumExactNumbersInNumericFieldsPlaybook.getExpectedHttpCodeWhenRequiredFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
    }

    @Test
    void shouldGenerateNumberBoundaryValue() {
        IntegerSchema schema = new IntegerSchema();
        schema.setMaximum(BigDecimal.ONE);
        Object generated = maximumExactNumbersInNumericFieldsPlaybook.getBoundaryValue(schema);

        Assertions.assertThat(generated).isInstanceOf(Number.class);
    }

    @ParameterizedTest
    @CsvSource({",true", "mama,true"})
    void shouldTestBoundaryDefinedBasedOnFormat(String format, boolean expected) {
        StringSchema stringSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", stringSchema)).build();
        stringSchema.setMaximum(BigDecimal.TEN);
        stringSchema.setFormat(format);
        Assertions.assertThat(maximumExactNumbersInNumericFieldsPlaybook.hasBoundaryDefined("test", data)).isEqualTo(expected);
    }
}