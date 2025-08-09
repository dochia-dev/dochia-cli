package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.NumberSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

@QuarkusTest
class ExtremePositiveNumbersInDecimalFieldsPlaybookTest
 {

    private ExtremePositiveNumbersInDecimalFieldsPlaybook extremePositiveNumbersInDecimalFieldsPlaybook;

    @BeforeEach
    void setup() {
        extremePositiveNumbersInDecimalFieldsPlaybook = new ExtremePositiveNumbersInDecimalFieldsPlaybook(null, null, null);
    }

    @Test
    void givenANewExtremePositiveValueDecimalFieldsPlaybook_whenCreatingANewInstance_thenTheMethodsBeingOverriddenAreMatchingTheDecimalPlaybook() {
        NumberSchema nrSchema = new NumberSchema();
        Assertions.assertThat(extremePositiveNumbersInDecimalFieldsPlaybook.getSchemaTypesThePlaybookWillApplyTo().stream().anyMatch(schema -> schema.equalsIgnoreCase("number"))).isTrue();
        Assertions.assertThat(extremePositiveNumbersInDecimalFieldsPlaybook.hasBoundaryDefined("test", PlaybookData.builder().build())).isTrue();
        Assertions.assertThat(extremePositiveNumbersInDecimalFieldsPlaybook.description()).isNotNull();
        Assertions.assertThat(extremePositiveNumbersInDecimalFieldsPlaybook.typeOfDataSentToTheService()).isNotNull();
        Assertions.assertThat(extremePositiveNumbersInDecimalFieldsPlaybook.getBoundaryValue(nrSchema)).isInstanceOf(BigDecimal.class);
    }
}
