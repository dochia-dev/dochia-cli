package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.Collections;

@QuarkusTest
class MinLengthExactValuesInStringFieldsPlaybookTest
 {

    private MinLengthExactValuesInStringFieldsPlaybook minLengthExactValuesInStringFieldsPlaybook;

    @BeforeEach
    void setup() {
        FilesArguments filesArguments = Mockito.mock(FilesArguments.class);
        minLengthExactValuesInStringFieldsPlaybook = new MinLengthExactValuesInStringFieldsPlaybook(null, null, filesArguments);
        Mockito.when(filesArguments.getRefData(Mockito.anyString())).thenReturn(Collections.emptyMap());
    }

    @Test
    void shouldApply() {
        Assertions.assertThat(minLengthExactValuesInStringFieldsPlaybook.getSchemaTypesThePlaybookWillApplyTo()).containsOnly("string");
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(minLengthExactValuesInStringFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldNotHaveBoundaryDefined() {
        StringSchema stringSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", stringSchema)).build();
        Assertions.assertThat(minLengthExactValuesInStringFieldsPlaybook.hasBoundaryDefined("test", data)).isFalse();
    }

    @Test
    void shouldHaveBoundaryDefined() {
        StringSchema stringSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", stringSchema)).build();
        stringSchema.setMinLength(20);
        Assertions.assertThat(minLengthExactValuesInStringFieldsPlaybook.hasBoundaryDefined("test", data)).isTrue();
    }

    @Test
    void shouldSkipForDeleteAndGet() {
        Assertions.assertThat(minLengthExactValuesInStringFieldsPlaybook.skipForHttpMethods()).containsOnly(HttpMethod.GET, HttpMethod.DELETE);
    }

    @Test
    void shouldReturnDataToSendToTheService() {
        Assertions.assertThat(minLengthExactValuesInStringFieldsPlaybook.typeOfDataSentToTheService()).isEqualTo("exact minLength size values");
    }

    @Test
    void shouldHaveBoundaryValue() {
        StringSchema stringSchema = new StringSchema();
        stringSchema.setMinLength(20);
        Assertions.assertThat(minLengthExactValuesInStringFieldsPlaybook.getBoundaryValue(stringSchema)).isNotNull();
    }

    @Test
    void shouldReturn2XXForExpectedResultCodes() {
        Assertions.assertThat(minLengthExactValuesInStringFieldsPlaybook.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(minLengthExactValuesInStringFieldsPlaybook.getExpectedHttpCodeWhenRequiredFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
    }

    @ParameterizedTest
    @CsvSource({",true", "mama,false"})
    void shouldTestBoundaryDefinedBasedOnFormat(String format, boolean expected) {
        StringSchema stringSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", stringSchema)).build();
        stringSchema.setMinLength(20);
        stringSchema.setFormat(format);
        Assertions.assertThat(minLengthExactValuesInStringFieldsPlaybook.hasBoundaryDefined("test", data)).isEqualTo(expected);
    }
}
