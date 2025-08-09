package dev.dochia.cli.core.playbook.field.base;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.http.ResponseCodeFamily;
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
class ExpectOnly2XXBaseFieldsPlaybookTest
 {

    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;

    private ExpectOnly2XXBaseFieldsPlaybook expectOnly2XXBaseFieldsPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        expectOnly2XXBaseFieldsPlaybook = new CustomExpect2XX(serviceCaller, testCaseListener, filesArguments);
    }

    @Test
    void givenADummyExpectOnly2XXFieldsPlaybook_whenCreatingANewInstance_thenTheDefaultMethodsAreMatchingThe2XXPlaybook() {
        Assertions.assertThat(expectOnly2XXBaseFieldsPlaybook.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(expectOnly2XXBaseFieldsPlaybook.getExpectedHttpCodeWhenRequiredFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(expectOnly2XXBaseFieldsPlaybook).hasToString(expectOnly2XXBaseFieldsPlaybook.getClass().getSimpleName());
    }

    static class CustomExpect2XX extends ExpectOnly2XXBaseFieldsPlaybook {

        public CustomExpect2XX(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
            super(sc, lr, cp);
        }

        @Override
        protected String typeOfDataSentToTheService() {
            return null;
        }

        @Override
        public ResponseCodeFamily getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern() {
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
