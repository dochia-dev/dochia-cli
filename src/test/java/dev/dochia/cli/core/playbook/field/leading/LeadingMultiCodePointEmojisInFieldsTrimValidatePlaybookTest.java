package dev.dochia.cli.core.playbook.field.leading;

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
class LeadingMultiCodePointEmojisInFieldsTrimValidatePlaybookTest
 {
    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;
    private LeadingMultiCodePointEmojisInFieldsTrimValidatePlaybook leadingMultiCodePointEmojisInFieldsTrimValidatePlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        leadingMultiCodePointEmojisInFieldsTrimValidatePlaybook = new LeadingMultiCodePointEmojisInFieldsTrimValidatePlaybook(serviceCaller, testCaseListener, filesArguments);
        Mockito.when(testCaseListener.isFieldNotADiscriminator(Mockito.anyString())).thenReturn(true);
        Mockito.when(testCaseListener.isFieldNotADiscriminator("pet#type")).thenReturn(false);
    }

    @Test
    void shouldProperlyOverrideMethods() {
        FuzzingStrategy fuzzingStrategy = leadingMultiCodePointEmojisInFieldsTrimValidatePlaybook.getFieldFuzzingStrategy(null, null).getFirst();
        Assertions.assertThat(fuzzingStrategy.name()).isEqualTo(FuzzingStrategy.prefix().name());

        Assertions.assertThat(fuzzingStrategy.getData()).isEqualTo("\uD83D\uDC69\uD83C\uDFFE");
        Assertions.assertThat(leadingMultiCodePointEmojisInFieldsTrimValidatePlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(leadingMultiCodePointEmojisInFieldsTrimValidatePlaybook.description()).isNotNull();
        Assertions.assertThat(leadingMultiCodePointEmojisInFieldsTrimValidatePlaybook.typeOfDataSentToTheService()).isNotNull();
    }

    @Test
    void shouldNotRunIfDiscriminatorField() {
        Assertions.assertThat(leadingMultiCodePointEmojisInFieldsTrimValidatePlaybook.isPlaybookApplicable(null, "pet#type")).isFalse();
    }

    @Test
    void shouldRunIfNotDiscriminatorField() {
        Assertions.assertThat(leadingMultiCodePointEmojisInFieldsTrimValidatePlaybook.isPlaybookApplicable(null, "pet#number")).isTrue();
    }
}
