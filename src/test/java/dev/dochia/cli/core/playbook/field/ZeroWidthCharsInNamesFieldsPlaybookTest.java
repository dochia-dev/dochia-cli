package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.http.HttpMethod;
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
class ZeroWidthCharsInNamesFieldsPlaybookTest
 {
    private ZeroWidthCharsInNamesFieldsPlaybook zeroWidthCharsInNamesFieldsPlaybook;
    private ServiceCaller serviceCaller;
    @InjectSpy
    TestCaseListener testCaseListener;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        zeroWidthCharsInNamesFieldsPlaybook = new ZeroWidthCharsInNamesFieldsPlaybook(serviceCaller, testCaseListener);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(zeroWidthCharsInNamesFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(zeroWidthCharsInNamesFieldsPlaybook).hasToString(zeroWidthCharsInNamesFieldsPlaybook.getClass().getSimpleName());
    }

    @ParameterizedTest
    @CsvSource(value = {"''", "null"}, nullValues = "null")
    void shouldNotRunWithEmptyPayload(String payload) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getPayload()).thenReturn(payload);
        zeroWidthCharsInNamesFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(serviceCaller);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldNotRunWhenNoFields() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of());
        zeroWidthCharsInNamesFieldsPlaybook.run(data);
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
        zeroWidthCharsInNamesFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(18)).reportResult(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX));
    }

    @Test
    void shouldSkipForHttpMethods() {
        Assertions.assertThat(zeroWidthCharsInNamesFieldsPlaybook.skipForHttpMethods()).containsOnly(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.TRACE);
    }
}
