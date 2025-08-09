package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

@QuarkusTest
class ZeroWidthCharsInValuesFieldsSanitizeValidatePlaybookTest
 {
    private ZeroWidthCharsInValuesFieldsSanitizeValidatePlaybook zeroWidthCharsInValuesFieldsSanitizeValidatePlaybook;
    private ServiceCaller serviceCaller;
    @InjectSpy
    TestCaseListener testCaseListener;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        zeroWidthCharsInValuesFieldsSanitizeValidatePlaybook = new ZeroWidthCharsInValuesFieldsSanitizeValidatePlaybook(serviceCaller, testCaseListener, Mockito.mock(FilesArguments.class));
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(zeroWidthCharsInValuesFieldsSanitizeValidatePlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(zeroWidthCharsInValuesFieldsSanitizeValidatePlaybook).hasToString(zeroWidthCharsInValuesFieldsSanitizeValidatePlaybook.getClass().getSimpleName());
    }

    @ParameterizedTest
    @CsvSource(value = {"''", "null"}, nullValues = "null")
    void shouldNotRunWithEmptyPayload(String payload) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getPayload()).thenReturn(payload);
        zeroWidthCharsInValuesFieldsSanitizeValidatePlaybook.run(data);
        Mockito.verifyNoInteractions(serviceCaller);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldNotRunWhenNoFields() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of());
        zeroWidthCharsInValuesFieldsSanitizeValidatePlaybook.run(data);
        Mockito.verifyNoInteractions(serviceCaller);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldRunWhenFieldsPresent() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("name", "lastName", "address#zip"));
        Mockito.when(data.getPayload()).thenReturn("""
                {
                    "name": "John",
                    "lastName": "Doe",
                    "address": {
                        "zip": "12345"
                    }
                }
                """);
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().responseCode(400).build());
        zeroWidthCharsInValuesFieldsSanitizeValidatePlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(18)).reportResult(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX), Mockito.eq(true), Mockito.eq(true));
    }

    @Test
    void shouldNotRunWhenFieldIsADiscriminator() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("name", "lastName", "address#zip"));
        Mockito.when(data.getPayload()).thenReturn("""
                {
                    "name": "John",
                    "lastName": "Doe",
                    "address": {
                        "zip": "12345"
                    }
                }
                """);
        Mockito.when(testCaseListener.isFieldNotADiscriminator(Mockito.anyString())).thenReturn(false);
        zeroWidthCharsInValuesFieldsSanitizeValidatePlaybook.run(data);
        Mockito.verifyNoInteractions(serviceCaller);
    }
}
