package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

@QuarkusTest
class NullUnicodeBodyPlaybookTest {
    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private NullUnicodeBodyPlaybook nullUnicodeBodyPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        SimpleExecutor simpleExecutor = new SimpleExecutor(testCaseListener, serviceCaller);
        nullUnicodeBodyPlaybook = new NullUnicodeBodyPlaybook(simpleExecutor);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void shouldNotRunForEmptyPayload() {
        nullUnicodeBodyPlaybook.run(Mockito.mock(PlaybookData.class));

        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void givenAHttpMethodWithPayload_whenApplyingTheMalformedJsonPlaybook_thenTheResultsAreCorrectlyReported() {
        PlaybookData data = PlaybookData.builder().method(HttpMethod.POST).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).responseCodes(Set.of("400")).build();
        ReflectionTestUtils.setField(data, "processedPayload", "{\"id\": 1}");

        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(400).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any(), Mockito.anyBoolean());

        nullUnicodeBodyPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX), Mockito.anyBoolean(), Mockito.eq(true));
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(nullUnicodeBodyPlaybook).hasToString(nullUnicodeBodyPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(nullUnicodeBodyPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldSkipForNonHttpBodyMethods() {
        Assertions.assertThat(nullUnicodeBodyPlaybook.skipForHttpMethods()).contains(HttpMethod.GET, HttpMethod.DELETE);
    }

    @Test
    void shouldHaveNullPayload() {
        Assertions.assertThat(nullUnicodeBodyPlaybook.getPayload(null)).isEqualTo("\u0000");
    }
}
