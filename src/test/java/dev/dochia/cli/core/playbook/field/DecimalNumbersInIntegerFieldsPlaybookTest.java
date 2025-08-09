package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.NumberSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class DecimalNumbersInIntegerFieldsPlaybookTest
 {

    private DecimalNumbersInIntegerFieldsPlaybook decimalNumbersInIntegerFieldsPlaybook;

    @BeforeEach
    void setup() {
        decimalNumbersInIntegerFieldsPlaybook = new DecimalNumbersInIntegerFieldsPlaybook(null, null, null);
    }

    @Test
    void givenANewDecimalFieldsPlaybook_whenCreatingANewInstance_thenTheMethodsBeingOverriddenAreMatchingTheDecimalPlaybook() {
        NumberSchema nrSchema = new NumberSchema();
        Assertions.assertThat(decimalNumbersInIntegerFieldsPlaybook.getSchemaTypesThePlaybookWillApplyTo().stream().anyMatch(schema -> schema.equalsIgnoreCase("integer"))).isTrue();
        Assertions.assertThat(decimalNumbersInIntegerFieldsPlaybook.hasBoundaryDefined("test", PlaybookData.builder().build())).isTrue();
        Assertions.assertThat(decimalNumbersInIntegerFieldsPlaybook.description()).isNotNull();
        Assertions.assertThat(decimalNumbersInIntegerFieldsPlaybook.typeOfDataSentToTheService()).isNotNull();
        Assertions.assertThat(decimalNumbersInIntegerFieldsPlaybook.getBoundaryValue(nrSchema)).isInstanceOf(Double.class);
    }
}
