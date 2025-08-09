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
class TrailingSingleCodePointEmojisInFieldsTrimValidatePlaybookTest
 {
    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;
    private TrailingSingleCodePointEmojisInFieldsTrimValidatePlaybook trailingSingleCodePointEmojisInFieldsTrimValidatePlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        trailingSingleCodePointEmojisInFieldsTrimValidatePlaybook = new TrailingSingleCodePointEmojisInFieldsTrimValidatePlaybook(serviceCaller, testCaseListener, filesArguments);
        Mockito.when(testCaseListener.isFieldNotADiscriminator(Mockito.anyString())).thenReturn(true);
        Mockito.when(testCaseListener.isFieldNotADiscriminator("pet#type")).thenReturn(false);
    }

    @Test
    void givenANewTrailingTabsInFieldsTrimValidatePlaybook_whenCreatingANewInstance_thenTheMethodsBeingOverriddenAreMatchingTheTrailingTabsInFieldsTrimValidatePlaybook() {
        FuzzingStrategy fuzzingStrategy = trailingSingleCodePointEmojisInFieldsTrimValidatePlaybook.getFieldFuzzingStrategy(null, null).get(1);
        Assertions.assertThat(fuzzingStrategy.name()).isEqualTo(FuzzingStrategy.trail().name());

        Assertions.assertThat(fuzzingStrategy.getData()).isEqualTo("\uD83D\uDC80");
        Assertions.assertThat(trailingSingleCodePointEmojisInFieldsTrimValidatePlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(trailingSingleCodePointEmojisInFieldsTrimValidatePlaybook.description()).isNotNull();
        Assertions.assertThat(trailingSingleCodePointEmojisInFieldsTrimValidatePlaybook.typeOfDataSentToTheService()).isNotNull();
    }

    @Test
    void shouldNotRunIfDiscriminatorField() {
        Assertions.assertThat(trailingSingleCodePointEmojisInFieldsTrimValidatePlaybook.isPlaybookApplicable(null, "pet#type")).isFalse();
    }

    @Test
    void shouldRunIfNotDiscriminatorField() {
        Assertions.assertThat(trailingSingleCodePointEmojisInFieldsTrimValidatePlaybook.isPlaybookApplicable(null, "pet#number")).isTrue();
    }
}
