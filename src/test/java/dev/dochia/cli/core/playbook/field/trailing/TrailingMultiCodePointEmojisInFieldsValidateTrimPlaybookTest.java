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
class TrailingMultiCodePointEmojisInFieldsValidateTrimPlaybookTest
 {
    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;
    private TrailingMultiCodePointEmojisInFieldsValidateTrimPlaybook trailingMultiCodePointEmojisInFieldsValidateTrimPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        trailingMultiCodePointEmojisInFieldsValidateTrimPlaybook = new TrailingMultiCodePointEmojisInFieldsValidateTrimPlaybook(serviceCaller, testCaseListener, filesArguments);
        Mockito.when(testCaseListener.isFieldNotADiscriminator(Mockito.anyString())).thenReturn(true);
        Mockito.when(testCaseListener.isFieldNotADiscriminator("pet#type")).thenReturn(false);
    }

    @Test
    void shouldProperlyOverrideMethods() {
        FuzzingStrategy fuzzingStrategy = trailingMultiCodePointEmojisInFieldsValidateTrimPlaybook.getFieldFuzzingStrategy(null, null).get(1);
        Assertions.assertThat(fuzzingStrategy.name()).isEqualTo(FuzzingStrategy.trail().name());

        Assertions.assertThat(fuzzingStrategy.getData()).isEqualTo("\uD83D\uDC68\u200D\uD83C\uDFED️");
        Assertions.assertThat(trailingMultiCodePointEmojisInFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(trailingMultiCodePointEmojisInFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(trailingMultiCodePointEmojisInFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenRequiredFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);

        Assertions.assertThat(trailingMultiCodePointEmojisInFieldsValidateTrimPlaybook.description()).isNotNull();
        Assertions.assertThat(trailingMultiCodePointEmojisInFieldsValidateTrimPlaybook.typeOfDataSentToTheService()).isNotNull();
    }

    @Test
    void shouldNotRunIfDiscriminatorField() {
        Assertions.assertThat(trailingMultiCodePointEmojisInFieldsValidateTrimPlaybook.isPlaybookApplicable(null, "pet#type")).isFalse();
    }

    @Test
    void shouldRunIfNotDiscriminatorField() {
        Assertions.assertThat(trailingMultiCodePointEmojisInFieldsValidateTrimPlaybook.isPlaybookApplicable(null, "pet#number")).isTrue();
    }
}
