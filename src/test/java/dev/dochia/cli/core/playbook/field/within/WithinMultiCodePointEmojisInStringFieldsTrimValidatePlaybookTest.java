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
class WithinMultiCodePointEmojisInStringFieldsTrimValidatePlaybookTest
 {
    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;
    private WithinMultiCodePointEmojisInStringFieldsTrimValidatePlaybook withinMultiCodePointEmojisInStringFieldsTrimValidatePlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        withinMultiCodePointEmojisInStringFieldsTrimValidatePlaybook = new WithinMultiCodePointEmojisInStringFieldsTrimValidatePlaybook(serviceCaller, testCaseListener, filesArguments);
        Mockito.when(testCaseListener.isFieldNotADiscriminator(Mockito.anyString())).thenReturn(true);
        Mockito.when(testCaseListener.isFieldNotADiscriminator("pet#type")).thenReturn(false);
    }

    @Test
    void shouldProperlyOverrideSuperClassMethods() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Map<String, Schema> reqTypes = new HashMap<>();
        reqTypes.put("field", new StringSchema());
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(reqTypes);
        FuzzingStrategy fuzzingStrategy = withinMultiCodePointEmojisInStringFieldsTrimValidatePlaybook.getFieldFuzzingStrategy(data, "field").get(1);
        Assertions.assertThat(fuzzingStrategy.name()).isEqualTo(FuzzingStrategy.replace().name());

        Assertions.assertThat(fuzzingStrategy.getData().toString()).contains("\uD83D\uDC68\u200D\uD83C\uDFED️");
        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsTrimValidatePlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsTrimValidatePlaybook.description()).isNotNull();
        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsTrimValidatePlaybook.concreteFuzzStrategy().name()).isEqualTo(FuzzingStrategy.replace().name());

        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsTrimValidatePlaybook.typeOfDataSentToTheService()).isNotNull();
    }

    @Test
    void shouldNotRunIfDiscriminatorField() {
        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsTrimValidatePlaybook.isPlaybookApplicable(null, "pet#type")).isFalse();
    }

    @Test
    void shouldRunIfNotDiscriminatorField() {
        Assertions.assertThat(withinMultiCodePointEmojisInStringFieldsTrimValidatePlaybook.isPlaybookApplicable(null, "pet#number")).isTrue();
    }
}
