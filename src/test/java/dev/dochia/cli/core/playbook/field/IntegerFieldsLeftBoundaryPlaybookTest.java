package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
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
class IntegerFieldsLeftBoundaryPlaybookTest
 {
    private IntegerFieldsLeftBoundaryPlaybook integerFieldsLeftBoundaryPlaybook;
    private FilesArguments filesArguments;

    @BeforeEach
    void setup() {
        filesArguments = Mockito.mock(FilesArguments.class);
        integerFieldsLeftBoundaryPlaybook = new IntegerFieldsLeftBoundaryPlaybook(null, null, filesArguments);
    }

    @Test
    void shouldApplyToInteger() {
        Assertions.assertThat(integerFieldsLeftBoundaryPlaybook.getSchemaTypesThePlaybookWillApplyTo()
                        .stream().anyMatch(schema -> schema.equalsIgnoreCase("integer")))
                .isTrue();
    }

    @ParameterizedTest
    @CsvSource({"string", "number", "date"})
    void shouldNotApplyTo(String format) {
        Assertions.assertThat(integerFieldsLeftBoundaryPlaybook.getSchemaTypesThePlaybookWillApplyTo()
                        .stream().anyMatch(schema -> schema.equalsIgnoreCase(format)))
                .isFalse();
    }

    @Test
    void shouldHaveBoundaryDefined() {
        Assertions.assertThat(integerFieldsLeftBoundaryPlaybook.hasBoundaryDefined("test", PlaybookData.builder().build())).isTrue();
    }

    @Test
    void shouldReturnNonNullDescription() {
        Assertions.assertThat(integerFieldsLeftBoundaryPlaybook.description()).isNotEmpty();
    }

    @Test
    void shouldReturnLongBoundaryValue() {
        NumberSchema nrSchema = new NumberSchema();
        Assertions.assertThat(integerFieldsLeftBoundaryPlaybook.getBoundaryValue(nrSchema)).isInstanceOf(Long.class);
    }

    @ParameterizedTest
    @CsvSource({"field,field,false", "field,notRefData,true"})
    void shouldCheckSkip(String field, String refData, boolean expected) {
        Mockito.when(filesArguments.getRefData(Mockito.any())).thenReturn(Map.of(refData, "value"));
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of(field, new StringSchema()));
        boolean isPossible = integerFieldsLeftBoundaryPlaybook.isPlaybookApplicable(data, field);
        Assertions.assertThat(isPossible).isEqualTo(expected);
    }
}
