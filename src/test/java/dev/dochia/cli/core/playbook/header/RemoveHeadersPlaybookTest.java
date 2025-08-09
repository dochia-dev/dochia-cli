package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.DochiaHeader;
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

import java.util.*;

@QuarkusTest
class RemoveHeadersPlaybookTest {
    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private RemoveHeadersPlaybook removeHeadersPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        SimpleExecutor simpleExecutor = new SimpleExecutor(testCaseListener, serviceCaller);
        removeHeadersPlaybook = new RemoveHeadersPlaybook(simpleExecutor);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void givenASetOfHeadersWithNoRequiredHeaders_whenApplyingTheRemoveHeadersPlaybook_thenTheHeadersAreProperlyFuzzed() {
        Map<String, List<String>> responses = new HashMap<>();
        responses.put("200", Collections.singletonList("response"));
        PlaybookData data = PlaybookData.builder().headers(Collections.singleton(DochiaHeader.builder().name("header").value("value").build())).
                responses(responses).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean());

        removeHeadersPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(2)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX), Mockito.anyBoolean(), Mockito.eq(true));
    }

    @Test
    void givenASetOfHeadersWithRequiredHeaders_whenApplyingTheRemoveHeadersPlaybook_thenTheHeadersAreProperlyFuzzed() {
        Map<String, List<String>> responses = new HashMap<>();
        responses.put("400", Collections.singletonList("response"));
        PlaybookData data = PlaybookData.builder().headers(Collections.singleton(DochiaHeader.builder().name("header").value("value").required(true).build())).
                responses(responses).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean());

        removeHeadersPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX), Mockito.anyBoolean(), Mockito.eq(true));
        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX), Mockito.anyBoolean(), Mockito.eq(true));
    }

    @Test
    void givenASetOfHeaders_whenAnErrorOccursCallingTheService_thenTheErrorIsProperlyReported() {
        PlaybookData data = PlaybookData.builder().headers(Collections.singleton(DochiaHeader.builder().name("header").value("value").build()))
                .reqSchema(new StringSchema()).requestContentTypes(List.of("application/json"))
                .responseCodes(Set.of("200")).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenThrow(new RuntimeException("this is deliberately thrown for testing"));
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any());

        removeHeadersPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(2)).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any());
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(removeHeadersPlaybook).hasToString(removeHeadersPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(removeHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldNotSkipForAnyHttpMethod() {
        Assertions.assertThat(removeHeadersPlaybook.skipForHttpMethods()).isEmpty();
    }

    @Test
    void shouldNotRunWhenNoHeaders() {
        PlaybookData data = PlaybookData.builder().headers(Collections.emptySet()).build();
        removeHeadersPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }
}
