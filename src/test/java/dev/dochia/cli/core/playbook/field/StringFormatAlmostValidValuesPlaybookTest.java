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
class StringFormatAlmostValidValuesPlaybookTest
 {
    private StringFormatAlmostValidValuesPlaybook stringFormatAlmostValidValuesPlaybook;

    @Inject
    InvalidDataFormat invalidDataFormat;

    @BeforeEach
    void setup() {
        stringFormatAlmostValidValuesPlaybook = new StringFormatAlmostValidValuesPlaybook(null, null, null, invalidDataFormat);
    }

    @Test
    void givenANewStringFormatAlmostValidValuesPlaybook_whenCreatingANewInstance_thenTheMethodsBeingOverriddenAreMatchingTheStringFormatAlmostValidValuesPlaybook() {
        NumberSchema nrSchema = new NumberSchema();
        nrSchema.setFormat("email");
        Assertions.assertThat(stringFormatAlmostValidValuesPlaybook.getSchemaTypesThePlaybookWillApplyTo()).containsExactly("string");
        Assertions.assertThat(stringFormatAlmostValidValuesPlaybook.getBoundaryValue(nrSchema)).isNotNull();
        Assertions.assertThat(stringFormatAlmostValidValuesPlaybook.hasBoundaryDefined("test", PlaybookData.builder().build())).isTrue();
        Assertions.assertThat(stringFormatAlmostValidValuesPlaybook.description()).isNotNull();
        Assertions.assertThat(stringFormatAlmostValidValuesPlaybook.typeOfDataSentToTheService()).isNotNull();

    }
}
