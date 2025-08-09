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
class WithinSingleCodePointEmojisInStringFieldsTrimValidatePlaybookTest
 {
    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;
    private WithinSingleCodePointEmojisInStringFieldsTrimValidatePlaybook withinSingleCodePointEmojisInStringFieldsTrimValidatePlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        withinSingleCodePointEmojisInStringFieldsTrimValidatePlaybook = new WithinSingleCodePointEmojisInStringFieldsTrimValidatePlaybook(serviceCaller, testCaseListener, filesArguments);
        Mockito.when(testCaseListener.isFieldNotADiscriminator(Mockito.anyString())).thenReturn(true);
        Mockito.when(testCaseListener.isFieldNotADiscriminator("pet#type")).thenReturn(false);
    }

    @Test
    void shouldProperlyOverrideSuperClassMethods() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Map<String, Schema> reqTypes = new HashMap<>();
        reqTypes.put("field", new StringSchema());
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(reqTypes);
        FuzzingStrategy fuzzingStrategy = withinSingleCodePointEmojisInStringFieldsTrimValidatePlaybook.getFieldFuzzingStrategy(data, "field").get(1);
        Assertions.assertThat(fuzzingStrategy.name()).isEqualTo(FuzzingStrategy.replace().name());

        Assertions.assertThat(fuzzingStrategy.getData().toString()).contains("\uD83D\uDC80");
        Assertions.assertThat(withinSingleCodePointEmojisInStringFieldsTrimValidatePlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(withinSingleCodePointEmojisInStringFieldsTrimValidatePlaybook.description()).isNotNull();
        Assertions.assertThat(withinSingleCodePointEmojisInStringFieldsTrimValidatePlaybook.concreteFuzzStrategy().name()).isEqualTo(FuzzingStrategy.replace().name());

        Assertions.assertThat(withinSingleCodePointEmojisInStringFieldsTrimValidatePlaybook.typeOfDataSentToTheService()).isNotNull();
    }

    @Test
    void shouldNotRunIfDiscriminatorField() {
        Assertions.assertThat(withinSingleCodePointEmojisInStringFieldsTrimValidatePlaybook.isPlaybookApplicable(null, "pet#type")).isFalse();
    }

    @Test
    void shouldRunIfNotDiscriminatorField() {
        Assertions.assertThat(withinSingleCodePointEmojisInStringFieldsTrimValidatePlaybook.isPlaybookApplicable(null, "pet#number")).isTrue();
    }
}
