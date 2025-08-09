package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.generator.format.api.InvalidDataFormat;
import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.NumberSchema;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StringFormatTotallyWrongValuesPlaybookTest
 {
    private StringFormatTotallyWrongValuesPlaybook stringFormatTotallyWrongValuesPlaybook;
    @Inject
    InvalidDataFormat invalidDataFormat;

    @BeforeEach
    void setup() {
        stringFormatTotallyWrongValuesPlaybook = new StringFormatTotallyWrongValuesPlaybook(null, null, null, invalidDataFormat);
    }

    @Test
    void givenANewStringFormatTotallyWrongValuesPlaybookTest_whenCreatingANewInstance_thenTheMethodsBeingOverriddenAreMatchingTheStringFormatTotallyWrongValuesPlaybookTest() {
        NumberSchema nrSchema = new NumberSchema();
        nrSchema.setFormat("email");
        Assertions.assertThat(stringFormatTotallyWrongValuesPlaybook.getSchemaTypesThePlaybookWillApplyTo()).containsExactly("string");
        Assertions.assertThat(stringFormatTotallyWrongValuesPlaybook.getBoundaryValue(nrSchema)).isNotNull();
        Assertions.assertThat(stringFormatTotallyWrongValuesPlaybook.hasBoundaryDefined("test", PlaybookData.builder().build())).isTrue();
        Assertions.assertThat(stringFormatTotallyWrongValuesPlaybook.description()).isNotNull();
        Assertions.assertThat(stringFormatTotallyWrongValuesPlaybook.typeOfDataSentToTheService()).isNotNull();

    }
}
