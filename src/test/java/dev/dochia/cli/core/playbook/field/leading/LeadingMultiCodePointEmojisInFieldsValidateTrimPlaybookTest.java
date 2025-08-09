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
class LeadingMultiCodePointEmojisInFieldsValidateTrimPlaybookTest
 {
    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;
    private LeadingMultiCodePointEmojisInFieldsValidateTrimPlaybook leadingMultiCodePointEmojisInFieldsValidateTrimPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        leadingMultiCodePointEmojisInFieldsValidateTrimPlaybook = new LeadingMultiCodePointEmojisInFieldsValidateTrimPlaybook(serviceCaller, testCaseListener, filesArguments);
        Mockito.when(testCaseListener.isFieldNotADiscriminator(Mockito.anyString())).thenReturn(true);
        Mockito.when(testCaseListener.isFieldNotADiscriminator("pet#type")).thenReturn(false);
    }

    @Test
    void shouldProperlyOverrideMethods() {
        FuzzingStrategy fuzzingStrategy = leadingMultiCodePointEmojisInFieldsValidateTrimPlaybook.getFieldFuzzingStrategy(null, null).getFirst();
        Assertions.assertThat(fuzzingStrategy.name()).isEqualTo(FuzzingStrategy.prefix().name());

        Assertions.assertThat(fuzzingStrategy.getData()).isEqualTo("\uD83D\uDC69\uD83C\uDFFE");
        Assertions.assertThat(leadingMultiCodePointEmojisInFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(leadingMultiCodePointEmojisInFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(leadingMultiCodePointEmojisInFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenRequiredFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);

        Assertions.assertThat(leadingMultiCodePointEmojisInFieldsValidateTrimPlaybook.description()).isNotNull();
        Assertions.assertThat(leadingMultiCodePointEmojisInFieldsValidateTrimPlaybook.typeOfDataSentToTheService()).isNotNull();
    }


    @Test
    void shouldNotRunIfDiscriminatorField() {
        Assertions.assertThat(leadingMultiCodePointEmojisInFieldsValidateTrimPlaybook.isPlaybookApplicable(null, "pet#type")).isFalse();
    }

    @Test
    void shouldRunIfNotDiscriminatorField() {
        Assertions.assertThat(leadingMultiCodePointEmojisInFieldsValidateTrimPlaybook.isPlaybookApplicable(null, "pet#number")).isTrue();
    }
}
