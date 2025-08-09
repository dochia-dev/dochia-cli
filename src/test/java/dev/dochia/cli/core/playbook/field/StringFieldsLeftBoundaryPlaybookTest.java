package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

@QuarkusTest
class StringFieldsLeftBoundaryPlaybookTest
 {
    private StringFieldsLeftBoundaryPlaybook stringFieldsLeftBoundaryPlaybook;

    @BeforeEach
    void setup() {
        stringFieldsLeftBoundaryPlaybook = new StringFieldsLeftBoundaryPlaybook(null, null, null);
    }

    @Test
    void givenANewStringFieldsLeftBoundaryPlaybook_whenCreatingANewInstance_thenTheMethodsBeingOverriddenAreMatchingTheStringFieldsLeftBoundaryPlaybook() {
        NumberSchema nrSchema = new NumberSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", nrSchema)).build();
        Assertions.assertThat(stringFieldsLeftBoundaryPlaybook.getSchemaTypesThePlaybookWillApplyTo().stream().anyMatch(schema -> schema.equalsIgnoreCase("string"))).isTrue();
        Assertions.assertThat(stringFieldsLeftBoundaryPlaybook.getBoundaryValue(nrSchema)).isNotNull();
        Assertions.assertThat(stringFieldsLeftBoundaryPlaybook.hasBoundaryDefined("test", data)).isTrue();
        Assertions.assertThat(stringFieldsLeftBoundaryPlaybook.description()).isNotNull();

        nrSchema.setMinLength(2);
        Assertions.assertThat(stringFieldsLeftBoundaryPlaybook.hasBoundaryDefined("test", data)).isTrue();

    }

    @Test
    void shouldHaveBoundaryDefined() {
        StringSchema nrSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", nrSchema)).build();
        Assertions.assertThat(stringFieldsLeftBoundaryPlaybook.hasBoundaryDefined("test", data)).isTrue();
    }

}
