package dev.dochia.cli.core.playbook.field.within;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

@QuarkusTest
class WithinMultiCodePointEmojisInStringFieldsValidateTrimPlaybookTest
 {
    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;
    private WithinMultiCodePointEmojisInStringFieldsValidateTrimPlaybook withinMultiCodePointEmojisInStringFieldsValidateTrimPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        withinMultiCodePointEmojisInStringFieldsValidateTrimPlaybook = new WithinMultiCodePointEmojisInStringFieldsValidateTrimPlaybook(serviceCaller, testCaseListener, filesArguments);
        Mockito.when(testCaseListener.isFieldNotADiscriminator(Mockito.anyString())).thenReturn(true);
        Mockito.when(testCaseListener.isFieldNotADiscriminator("pet#type")).thenReturn(false);
    }

    @Test
    void shouldProperlyOverrideSuperClassMethods() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Map<String, Schema> reqTypes = new HashMap<>();
        reqTypes.put("field", new StringSchema());
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(reqTypes);
        FuzzingStrategy fuzzingStrategy = withinMultiCodePointEmojisInStringFieldsValidateTrimPlaybook.getFieldFuzzingStrategy(data, "field").get(1);

        Assertions.assertThat(fuzzingStrategy.getData().toString()).contains("\uD83D\uDC68\u200D\uD83C\uDFED️");
        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenRequiredFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);

        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsValidateTrimPlaybook.description()).isNotNull();
        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsValidateTrimPlaybook.typeOfDataSentToTheService()).isNotNull();
        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsValidateTrimPlaybook.concreteFuzzStrategy().name()).isEqualTo(FuzzingStrategy.replace().name());
    }

    @Test
    void shouldNotRunIfDiscriminatorField() {
        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsValidateTrimPlaybook.isPlaybookApplicable(null, "pet#type")).isFalse();
    }

    @Test
    void shouldRunIfNotDiscriminatorField() {
        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsValidateTrimPlaybook.isPlaybookApplicable(null, "pet#number")).isTrue();
    }
}
