package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

@QuarkusTest
class InvalidValuesInEnumsFieldsPlaybookTest
 {

    private InvalidValuesInEnumsFieldsPlaybook invalidValuesInEnumsFieldsPlaybook;
    private ProcessingArguments processingArguments;

    @BeforeEach
    void setup() {
        processingArguments = Mockito.mock(ProcessingArguments.class);
        invalidValuesInEnumsFieldsPlaybook = new InvalidValuesInEnumsFieldsPlaybook(null, null, null, processingArguments);
    }

    @Test
    void shouldNotHaveBoundaryDefined() {
        StringSchema stringSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", stringSchema)).build();
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.getSchemaTypesThePlaybookWillApplyTo().stream().anyMatch(schema -> schema.equalsIgnoreCase("string"))).isTrue();
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.getBoundaryValue(stringSchema)).isNullOrEmpty();
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.hasBoundaryDefined("test", data)).isFalse();
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.description()).isNotNull();
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.typeOfDataSentToTheService()).isNotNull();
    }

    @Test
    void shouldHaveBoundaryDefined() {
        StringSchema stringSchema = new StringSchema();
        stringSchema.setEnum(Collections.singletonList("TEST"));
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", stringSchema)).build();
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.getSchemaTypesThePlaybookWillApplyTo().stream().anyMatch(schema -> schema.equalsIgnoreCase("string"))).isTrue();
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.getBoundaryValue(stringSchema)).hasSizeLessThanOrEqualTo(4);
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.hasBoundaryDefined("test", data)).isTrue();
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.description()).isNotNull();
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.typeOfDataSentToTheService()).isNotNull();
    }

    @Test
    void shouldReturn4XXIfNotAllowInvalidEnumValues() {
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.getExpectedHttpCodeWhenRequiredFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
    }

    @Test
    void shouldReturn2XXIfAllowInvalidEnumValues() {
        Mockito.when(processingArguments.isAllowInvalidEnumValues()).thenReturn(true);
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.getExpectedHttpCodeWhenRequiredFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(invalidValuesInEnumsFieldsPlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
    }
}
