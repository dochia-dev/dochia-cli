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
class MaxLengthExactValuesInStringFieldsPlaybookTest
 {

    private MaxLengthExactValuesInStringFieldsPlaybook maxLengthExactValuesInStringFieldsPlaybook;

    @BeforeEach
    void setup() {
        FilesArguments filesArguments = Mockito.mock(FilesArguments.class);
        maxLengthExactValuesInStringFieldsPlaybook = new MaxLengthExactValuesInStringFieldsPlaybook(null, null, filesArguments);
        Mockito.when(filesArguments.getRefData(Mockito.anyString())).thenReturn(Collections.emptyMap());
    }


    @Test
    void shouldApply() {
        Assertions.assertThat(maxLengthExactValuesInStringFieldsPlaybook.getSchemaTypesThePlaybookWillApplyTo()).containsOnly("string");
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(maxLengthExactValuesInStringFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldNotHaveBoundaryDefined() {
        StringSchema stringSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", stringSchema)).build();
        Assertions.assertThat(maxLengthExactValuesInStringFieldsPlaybook.hasBoundaryDefined("test", data)).isFalse();
    }

    @Test
    void shouldHaveBoundaryDefined() {
        StringSchema stringSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", stringSchema)).build();
        stringSchema.setMaxLength(20);
        Assertions.assertThat(maxLengthExactValuesInStringFieldsPlaybook.hasBoundaryDefined("test", data)).isTrue();
    }

    @Test
    void shouldSkipForDeleteAndGet() {
        Assertions.assertThat(maxLengthExactValuesInStringFieldsPlaybook.skipForHttpMethods()).containsOnly(HttpMethod.GET, HttpMethod.DELETE);
    }

    @Test
    void shouldReturnDataToSendToTheService() {
        Assertions.assertThat(maxLengthExactValuesInStringFieldsPlaybook.typeOfDataSentToTheService()).isEqualTo("exact maxLength size values");
    }

    @Test
    void shouldHaveBoundaryValue() {
        StringSchema stringSchema = new StringSchema();
        stringSchema.setMaxLength(20);
        Assertions.assertThat(maxLengthExactValuesInStringFieldsPlaybook.getBoundaryValue(stringSchema)).isNotNull();
    }

    @Test
    void shouldReturn2XXForExpectedResultCodes() {
        Assertions.assertThat(maxLengthExactValuesInStringFieldsPlaybook.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(maxLengthExactValuesInStringFieldsPlaybook.getExpectedHttpCodeWhenRequiredFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
    }

    @ParameterizedTest
    @CsvSource({",true", "mama,false"})
    void shouldTestBoundaryDefinedBasedOnFormat(String format, boolean expected) {
        StringSchema stringSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", stringSchema)).build();
        stringSchema.setMaxLength(20);
        stringSchema.setFormat(format);
        Assertions.assertThat(maxLengthExactValuesInStringFieldsPlaybook.hasBoundaryDefined("test", data)).isEqualTo(expected);
    }
}
