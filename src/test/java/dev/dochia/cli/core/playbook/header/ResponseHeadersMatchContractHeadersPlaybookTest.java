package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import dev.dochia.cli.core.util.KeyValuePair;
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
class ResponseHeadersMatchContractHeadersPlaybookTest {
    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private ResponseHeadersMatchContractHeadersPlaybook responseHeadersMatchContractHeadersPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        SimpleExecutor simpleExecutor = new SimpleExecutor(testCaseListener, serviceCaller);
        responseHeadersMatchContractHeadersPlaybook = new ResponseHeadersMatchContractHeadersPlaybook(testCaseListener, simpleExecutor);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(responseHeadersMatchContractHeadersPlaybook.description()).isEqualTo("send a request with all fields and headers populated and checks if the response headers match the ones defined in the contract");
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(responseHeadersMatchContractHeadersPlaybook).hasToString(responseHeadersMatchContractHeadersPlaybook.getClass().getSimpleName());
    }


    @Test
    void shouldNotRunWhenNoResponseHeaders() {
        PlaybookData data = PlaybookData.builder().responseHeaders(Collections.emptyMap())
                .requestContentTypes(Collections.singletonList("application/json")).reqSchema(new StringSchema()).build();
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(),
                Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX));
        Mockito.doNothing().when(testCaseListener).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any());

        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).headers(Collections.emptyList()).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        responseHeadersMatchContractHeadersPlaybook.run(data);

        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldReportMissingHeaders() {
        PlaybookData data = PlaybookData.builder().responseHeaders(Map.of("200", Set.of("missingHeader", "anotherMissingHeader")))
                .requestContentTypes(Collections.singletonList("application/json")).reqSchema(new StringSchema()).build();
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(),
                Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX));
        Mockito.doNothing().when(testCaseListener).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any());

        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).headers(List.of(new KeyValuePair<>("header1", "value1"))).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        responseHeadersMatchContractHeadersPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(),
                Mockito.eq("The following response headers defined in the contract are missing: {}"), Mockito.eq(new TreeSet<>(Set.of("anotherMissingHeader", "missingHeader")).toArray()));
    }

    @Test
    void shouldNotReportMissingHeaders() {
        PlaybookData data = PlaybookData.builder().responseHeaders(Map.of("200", Set.of("notMissing", "notMissingAlso")))
                .requestContentTypes(Collections.singletonList("application/json")).reqSchema(new StringSchema()).build();
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(),
                Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX));
        Mockito.doNothing().when(testCaseListener).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any());

        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).headers(List.of(new KeyValuePair<>("notMissing", "value1"), new KeyValuePair<>("notMissingAlso", "value2"))).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        responseHeadersMatchContractHeadersPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.any(), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX));

    }
}
