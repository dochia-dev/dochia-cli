package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.NumberSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ExtremeNegativeNumbersInIntegerFieldsPlaybookTest
 {

    private ExtremeNegativeNumbersInIntegerFieldsPlaybook extremeNegativeNumbersInIntegerFieldsPlaybook;

    @BeforeEach
    void setup() {
        extremeNegativeNumbersInIntegerFieldsPlaybook = new ExtremeNegativeNumbersInIntegerFieldsPlaybook(null, null, null);
    }

    @Test
    void givenANewExtremeNegativeValueIntegerFieldsPlaybook_whenCreatingANewInstance_thenTheMethodsBeingOverriddenAreMatchingTheIntegerPlaybook() {
        NumberSchema nrSchema = new NumberSchema();
        Assertions.assertThat(extremeNegativeNumbersInIntegerFieldsPlaybook.getSchemaTypesThePlaybookWillApplyTo().stream().anyMatch(schema -> schema.equalsIgnoreCase("integer"))).isTrue();
        Assertions.assertThat(extremeNegativeNumbersInIntegerFieldsPlaybook.hasBoundaryDefined("test", PlaybookData.builder().build())).isTrue();
        Assertions.assertThat(extremeNegativeNumbersInIntegerFieldsPlaybook.description()).isNotNull();
        Assertions.assertThat(extremeNegativeNumbersInIntegerFieldsPlaybook.typeOfDataSentToTheService()).isNotNull();
        Assertions.assertThat(extremeNegativeNumbersInIntegerFieldsPlaybook.getBoundaryValue(nrSchema)).isInstanceOf(Long.class);
    }
}
