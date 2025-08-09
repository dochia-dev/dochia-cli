package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

@QuarkusTest
class StringFieldsRightBoundaryPlaybookTest
 {
    private StringFieldsRightBoundaryPlaybook stringFieldsRightBoundaryPlaybook;

    @BeforeEach
    void setup() {
        stringFieldsRightBoundaryPlaybook = new StringFieldsRightBoundaryPlaybook(null, null, null);
    }

    @Test
    void givenANewStringFieldsRightBoundaryPlaybook_whenCreatingANewInstance_thenTheMethodsBeingOverriddenAreMatchingTheStringFieldsRightBoundaryPlaybook() {
        StringSchema nrSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", nrSchema)).build();
        Assertions.assertThat(stringFieldsRightBoundaryPlaybook.getSchemaTypesThePlaybookWillApplyTo().stream().anyMatch(schema -> schema.equalsIgnoreCase("string"))).isTrue();
        Assertions.assertThat(stringFieldsRightBoundaryPlaybook.getBoundaryValue(nrSchema)).isNotNull();
        Assertions.assertThat(stringFieldsRightBoundaryPlaybook.hasBoundaryDefined("test", data)).isTrue();
        Assertions.assertThat(stringFieldsRightBoundaryPlaybook.description()).isNotNull();

        nrSchema.setMaxLength(2);
        Assertions.assertThat(stringFieldsRightBoundaryPlaybook.hasBoundaryDefined("test", data)).isTrue();

    }

    @Test
    void shouldHaveBoundaryDefined() {
        StringSchema nrSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().requestPropertyTypes(Collections.singletonMap("test", nrSchema)).build();
        Assertions.assertThat(stringFieldsRightBoundaryPlaybook.hasBoundaryDefined("test", data)).isTrue();
    }

}
