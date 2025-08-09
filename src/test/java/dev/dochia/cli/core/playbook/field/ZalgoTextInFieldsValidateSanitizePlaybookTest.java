package dev.dochia.cli.core.playbook.field;

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
class ZalgoTextInFieldsValidateSanitizePlaybookTest
 {
    private ZalgoTextInFieldsValidateSanitizePlaybook zalgoTextInStringFieldsValidateSanitizePlaybook;

    @BeforeEach
    void setup() {
        ServiceCaller serviceCaller = Mockito.mock(ServiceCaller.class);
        TestCaseListener testCaseListener = Mockito.mock(TestCaseListener.class);
        FilesArguments filesArguments = Mockito.mock(FilesArguments.class);
        zalgoTextInStringFieldsValidateSanitizePlaybook = new ZalgoTextInFieldsValidateSanitizePlaybook(serviceCaller, testCaseListener, filesArguments);
        Mockito.when(testCaseListener.isFieldNotADiscriminator(Mockito.anyString())).thenReturn(true);
        Mockito.when(testCaseListener.isFieldNotADiscriminator("pet#type")).thenReturn(false);
    }

    @Test
    void shouldProperlyOverrideSuperClassMethods() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Map<String, Schema> reqTypes = new HashMap<>();
        reqTypes.put("field", new StringSchema());
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(reqTypes);
        FuzzingStrategy fuzzingStrategy = zalgoTextInStringFieldsValidateSanitizePlaybook.getFieldFuzzingStrategy(data, "field").getFirst();

        Assertions.assertThat(fuzzingStrategy.getData().toString()).contains(" ̵̡̡̢̡̨̨̢͚̬̱̤̰̗͉͚̖͙͎͔͔̺̳͕̫̬͚̹͖̬̭̖̪̗͕̜̣̥̣̼͍͉̖͍̪͈̖͚̙͛͒͂̎̊̿̀̅̈͌͋̃̾̈̾̇͛͌͘͜͜͠͝ͅͅͅ ̷͕̗̇͛̅̀̑̇̈͗͌͛̐̀͆̐̊̅̋̈́̂̈́̈́͑̓͂͂̌̈́̽͌͐̐͂͐̈́̍̂͗̂͘͠͝͝͝ͅ ".replace(" ", ""));
        Assertions.assertThat(zalgoTextInStringFieldsValidateSanitizePlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(zalgoTextInStringFieldsValidateSanitizePlaybook.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(zalgoTextInStringFieldsValidateSanitizePlaybook.getExpectedHttpCodeWhenRequiredFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(zalgoTextInStringFieldsValidateSanitizePlaybook.description()).isNotNull();
        Assertions.assertThat(zalgoTextInStringFieldsValidateSanitizePlaybook.typeOfDataSentToTheService()).isNotNull();
    }

    @Test
    void shouldNotRunIfDiscriminatorField() {
        Assertions.assertThat(zalgoTextInStringFieldsValidateSanitizePlaybook.isPlaybookApplicable(null, "pet#type")).isFalse();
    }

    @Test
    void shouldRunIfNotDiscriminatorField() {
        Assertions.assertThat(zalgoTextInStringFieldsValidateSanitizePlaybook.isPlaybookApplicable(null, "pet#number")).isTrue();
    }
}
