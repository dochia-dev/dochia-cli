package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkusTest
class AbugidasInStringFieldsSanitizeValidatePlaybookTest
 {

    private AbugidasInStringFieldsSanitizeValidatePlaybook abugidasCharsInStringFieldsSanitizeValidatePlaybook;
    private FilesArguments filesArguments;

    @BeforeEach
    void setup() {
        ServiceCaller serviceCaller = Mockito.mock(ServiceCaller.class);
        TestCaseListener testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        abugidasCharsInStringFieldsSanitizeValidatePlaybook = new AbugidasInStringFieldsSanitizeValidatePlaybook(serviceCaller, testCaseListener, filesArguments);
        Mockito.when(testCaseListener.isFieldNotADiscriminator(Mockito.anyString())).thenReturn(true);
        Mockito.when(testCaseListener.isFieldNotADiscriminator("pet#type")).thenReturn(false);
    }

    @Test
    void shouldGetReplaceFuzzingStrategy() {
        PlaybookData data = mockFuzzingData();
        FuzzingStrategy fuzzingStrategy = abugidasCharsInStringFieldsSanitizeValidatePlaybook.getFieldFuzzingStrategy(data, "field").getFirst();
        Assertions.assertThat(fuzzingStrategy.name()).isEqualTo(FuzzingStrategy.replace().name());
        Assertions.assertThat(fuzzingStrategy.getData().toString()).contains("జ్ఞ\u200Cా");
    }

    @Test
    void shouldReturnDescription() {
        Assertions.assertThat(abugidasCharsInStringFieldsSanitizeValidatePlaybook.description()).isNotBlank();
    }

    @Test
    void shouldReturn2XxWhenRunValueNotMatchingPattern() {
        Assertions.assertThat(abugidasCharsInStringFieldsSanitizeValidatePlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX_TWOXX);
    }

    @Test
    void shouldReturnTypeOfDataToSendToServices() {
        Assertions.assertThat(abugidasCharsInStringFieldsSanitizeValidatePlaybook.typeOfDataSentToTheService()).isNotBlank();
    }


    @NotNull
    private PlaybookData mockFuzzingData() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Map<String, Schema> reqTypes = new HashMap<>();
        StringSchema petAge = new StringSchema();
        petAge.setEnum(List.of("1", "2"));
        reqTypes.put("field", new StringSchema());
        reqTypes.put("pet#number", new StringSchema());
        reqTypes.put("pet#age", petAge);
        reqTypes.put("pet#size", new IntegerSchema());
        Mockito.when(data.getPath()).thenReturn("/test");
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(reqTypes);
        return data;
    }

    @Test
    void shouldNotRunIfDiscriminatorField() {
        Assertions.assertThat(abugidasCharsInStringFieldsSanitizeValidatePlaybook.isPlaybookApplicable(mockFuzzingData(), "pet#type")).isFalse();
    }

    @Test
    void shouldRunIfNotDiscriminatorField() {
        Assertions.assertThat(abugidasCharsInStringFieldsSanitizeValidatePlaybook.isPlaybookApplicable(mockFuzzingData(), "pet#number")).isTrue();
    }

    @Test
    void shouldNotRunIfRefDataField() {
        Map<String, Object> refData = Map.of("field", "test");
        Mockito.when(filesArguments.getRefData("/test")).thenReturn(refData);
        Assertions.assertThat(abugidasCharsInStringFieldsSanitizeValidatePlaybook.isPlaybookApplicable(mockFuzzingData(), "field")).isFalse();
    }

    @Test
    void shouldNotRunWhenEnum() {
        Map<String, Object> refData = Map.of("field", "test");
        Mockito.when(filesArguments.getRefData("/test")).thenReturn(refData);
        Assertions.assertThat(abugidasCharsInStringFieldsSanitizeValidatePlaybook.isPlaybookApplicable(mockFuzzingData(), "pet#age")).isFalse();
    }

    @Test
    void shouldNotRunWhenNotStringSchema() {
        Assertions.assertThat(abugidasCharsInStringFieldsSanitizeValidatePlaybook.getFieldFuzzingStrategy(mockFuzzingData(), "pet#size").getFirst().isSkip()).isTrue();

    }
}
