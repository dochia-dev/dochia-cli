package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.NumberSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ExtremePositiveNumbersInIntegerFieldsPlaybookTest
 {
    private ExtremePositiveNumbersInIntegerFieldsPlaybook extremePositiveValueInIntegerFields;

    @BeforeEach
    void setup() {
        extremePositiveValueInIntegerFields = new ExtremePositiveNumbersInIntegerFieldsPlaybook(null, null, null);
    }

    @Test
    void givenANewExtremePositiveValueIntegerFieldsPlaybook_whenCreatingANewInstance_thenTheMethodsBeingOverriddenAreMatchingTheIntegerPlaybook() {
        NumberSchema nrSchema = new NumberSchema();
        Assertions.assertThat(extremePositiveValueInIntegerFields.getSchemaTypesThePlaybookWillApplyTo().stream().anyMatch(schema -> schema.equalsIgnoreCase("integer"))).isTrue();
        Assertions.assertThat(extremePositiveValueInIntegerFields.hasBoundaryDefined("test", PlaybookData.builder().build())).isTrue();
        Assertions.assertThat(extremePositiveValueInIntegerFields.description()).isNotNull();
        Assertions.assertThat(extremePositiveValueInIntegerFields.typeOfDataSentToTheService()).isNotNull();
        Assertions.assertThat(extremePositiveValueInIntegerFields.getBoundaryValue(nrSchema)).isInstanceOf(Long.class);
    }
}
