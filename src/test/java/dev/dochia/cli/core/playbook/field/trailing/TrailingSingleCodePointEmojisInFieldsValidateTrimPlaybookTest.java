package dev.dochia.cli.core.playbook.field.trailing;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class TrailingSingleCodePointEmojisInFieldsValidateTrimPlaybookTest
 {
    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;
    private TrailingSingleCodePointEmojisInFieldsValidateTrimPlaybook trailingSingleCodePointEmojisInFieldsValidateTrimPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        trailingSingleCodePointEmojisInFieldsValidateTrimPlaybook = new TrailingSingleCodePointEmojisInFieldsValidateTrimPlaybook(serviceCaller, testCaseListener, filesArguments);
        Mockito.when(testCaseListener.isFieldNotADiscriminator(Mockito.anyString())).thenReturn(true);
        Mockito.when(testCaseListener.isFieldNotADiscriminator("pet#type")).thenReturn(false);
    }

    @Test
    void givenANewTrailingTabsInFieldsValidateTrimPlaybook_whenCreatingANewInstance_thenTheMethodsBeingOverriddenAreMatchingTheTrailingTabsInFieldsValidateTrimPlaybook() {
        FuzzingStrategy fuzzingStrategy = trailingSingleCodePointEmojisInFieldsValidateTrimPlaybook.getFieldFuzzingStrategy(null, null).get(1);
        Assertions.assertThat(fuzzingStrategy.name()).isEqualTo(FuzzingStrategy.trail().name());

        Assertions.assertThat(fuzzingStrategy.getData()).isEqualTo("\uD83D\uDC80");
        Assertions.assertThat(trailingSingleCodePointEmojisInFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(trailingSingleCodePointEmojisInFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(trailingSingleCodePointEmojisInFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenRequiredFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);

        Assertions.assertThat(trailingSingleCodePointEmojisInFieldsValidateTrimPlaybook.description()).isNotNull();
        Assertions.assertThat(trailingSingleCodePointEmojisInFieldsValidateTrimPlaybook.typeOfDataSentToTheService()).isNotNull();
    }

    @Test
    void shouldNotRunIfDiscriminatorField() {
        Assertions.assertThat(trailingSingleCodePointEmojisInFieldsValidateTrimPlaybook.isPlaybookApplicable(null, "pet#type")).isFalse();
    }

    @Test
    void shouldRunIfNotDiscriminatorField() {
        Assertions.assertThat(trailingSingleCodePointEmojisInFieldsValidateTrimPlaybook.isPlaybookApplicable(null, "pet#number")).isTrue();
    }
}
