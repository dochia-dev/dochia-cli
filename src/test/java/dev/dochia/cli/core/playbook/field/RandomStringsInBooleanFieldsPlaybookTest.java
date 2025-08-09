package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class RandomStringsInBooleanFieldsPlaybookTest
 {
    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;

    private RandomStringsInBooleanFieldsPlaybook randomStringsInBooleanFieldsPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        randomStringsInBooleanFieldsPlaybook = new RandomStringsInBooleanFieldsPlaybook(serviceCaller, testCaseListener, filesArguments);
    }

    @Test
    void givenANewBooleanFieldsPlaybook_whenCreatingANewInstance_thenTheMethodsBeingOverriddenAreMatchingTheBooleanPlaybook() {
        Assertions.assertThat(randomStringsInBooleanFieldsPlaybook.getSchemaTypesThePlaybookWillApplyTo().stream().anyMatch(schema -> schema.equalsIgnoreCase("boolean"))).isTrue();
        Assertions.assertThat(randomStringsInBooleanFieldsPlaybook.getBoundaryValue(null)).isNotNull();
        Assertions.assertThat(randomStringsInBooleanFieldsPlaybook.hasBoundaryDefined(null, PlaybookData.builder().build())).isTrue();
        Assertions.assertThat(randomStringsInBooleanFieldsPlaybook.description()).isNotNull();
    }
}
