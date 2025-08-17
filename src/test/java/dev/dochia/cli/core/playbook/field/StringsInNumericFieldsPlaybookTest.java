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
class StringsInNumericFieldsPlaybookTest {

    private StringsInNumericFieldsPlaybook stringsInNumericFieldsPlaybook;

    @BeforeEach
    void setup() {
        stringsInNumericFieldsPlaybook = new StringsInNumericFieldsPlaybook(Mockito.mock(ServiceCaller.class), Mockito.mock(TestCaseListener.class), Mockito.mock(FilesArguments.class));
    }

    @Test
    void shouldGetSchemasThatThePlaybookWillApplyTo() {
        Assertions.assertThat(stringsInNumericFieldsPlaybook.getSchemaTypesThePlaybookWillApplyTo()).containsOnly("integer", "number");
    }

    @Test
    void shouldGetTypeOfDataSentToTheService() {
        Assertions.assertThat(stringsInNumericFieldsPlaybook.typeOfDataSentToTheService()).isEqualTo("strings in numeric fields");
    }

    @Test
    void shouldReturnTrueForHasBoundaryDefined() {
        Assertions.assertThat(stringsInNumericFieldsPlaybook.hasBoundaryDefined(null, PlaybookData.builder().build())).isTrue();
    }

    @Test
    void shouldGetDescription() {
        Assertions.assertThat(stringsInNumericFieldsPlaybook.description()).isNotNull();
    }

    @Test
    void shouldGenerateRandomString() {
        Assertions.assertThat(stringsInNumericFieldsPlaybook.getBoundaryValue(null)).isNotBlank();
    }
}
