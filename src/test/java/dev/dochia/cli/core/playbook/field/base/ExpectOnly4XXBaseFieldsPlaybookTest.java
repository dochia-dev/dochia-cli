package dev.dochia.cli.core.playbook.field.base;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

@QuarkusTest
class ExpectOnly4XXBaseFieldsPlaybookTest
 {

    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;

    private ExpectOnly4XXBaseFieldsPlaybook expectOnly4XXBaseFieldsPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        expectOnly4XXBaseFieldsPlaybook = new CustomExpect4XX(serviceCaller, testCaseListener, filesArguments);
    }

    @Test
    void givenADummyExpectOnly4XXFieldsPlaybook_whenCreatingANewInstance_thenTheDefaultMethodsAreMatchingThe4XXPlaybook() {
        Assertions.assertThat(expectOnly4XXBaseFieldsPlaybook.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(expectOnly4XXBaseFieldsPlaybook.getExpectedHttpCodeWhenRequiredFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(expectOnly4XXBaseFieldsPlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(expectOnly4XXBaseFieldsPlaybook).hasToString(expectOnly4XXBaseFieldsPlaybook.getClass().getSimpleName());
    }

    static class CustomExpect4XX extends ExpectOnly4XXBaseFieldsPlaybook {

        public CustomExpect4XX(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
            super(sc, lr, cp);
        }

        @Override
        protected String typeOfDataSentToTheService() {
            return null;
        }

        @Override
        protected List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
            return null;
        }

        @Override
        public String description() {
            return null;
        }
    }
}

