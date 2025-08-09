package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Map;

@QuarkusTest
class DecimalFieldsLeftBoundaryPlaybookTest
 {

    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;

    private DecimalFieldsLeftBoundaryPlaybook decimalFieldsLeftBoundaryPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        decimalFieldsLeftBoundaryPlaybook = new DecimalFieldsLeftBoundaryPlaybook(serviceCaller, testCaseListener, filesArguments);
    }

    @Test
    void shouldApplyToNumber() {
        Assertions.assertThat(decimalFieldsLeftBoundaryPlaybook.getSchemaTypesThePlaybookWillApplyTo()
                        .stream().anyMatch(schema -> schema.equalsIgnoreCase("number")))
                .isTrue();
    }

    @ParameterizedTest
    @CsvSource({"string", "integer", "date"})
    void shouldNotApplyToString(String format) {
        Assertions.assertThat(decimalFieldsLeftBoundaryPlaybook.getSchemaTypesThePlaybookWillApplyTo()
                        .stream().anyMatch(schema -> schema.equalsIgnoreCase(format)))
                .isFalse();
    }

    @Test
    void shouldHaveBoundaryDefined() {
        Assertions.assertThat(decimalFieldsLeftBoundaryPlaybook.hasBoundaryDefined("test", PlaybookData.builder().build())).isTrue();
    }

    @Test
    void shouldReturnNonNullDescription() {
        Assertions.assertThat(decimalFieldsLeftBoundaryPlaybook.description()).isNotEmpty();
    }

    @Test
    void shouldReturnLongBoundaryValue() {
        NumberSchema nrSchema = new NumberSchema();
        Assertions.assertThat(decimalFieldsLeftBoundaryPlaybook.getBoundaryValue(nrSchema)).isInstanceOf(BigDecimal.class);
    }

    @ParameterizedTest
    @CsvSource({"field,field,false", "field,notRefData,true"})
    void shouldCheckSkip(String field, String refData, boolean expected) {
        Mockito.when(filesArguments.getRefData(Mockito.any())).thenReturn(Map.of(refData, "value"));
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of(field, new StringSchema()));
        boolean isPossible = decimalFieldsLeftBoundaryPlaybook.isPlaybookApplicable(data, field);
        Assertions.assertThat(isPossible).isEqualTo(expected);
    }
}
