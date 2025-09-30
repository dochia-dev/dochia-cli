package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import dev.dochia.cli.core.util.KeyValuePair;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.StringSchema;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

@QuarkusTest
class HttpMethodsPlaybookTest {
    @Inject
    HttpMethodPlaybookUtil httpMethodPlaybookUtil;
    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private HttpMethodsPlaybook httpMethodsPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        httpMethodsPlaybook = new HttpMethodsPlaybook(httpMethodPlaybookUtil);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        SimpleExecutor simpleExecutor = new SimpleExecutor(testCaseListener, serviceCaller);
        ReflectionTestUtils.setField(httpMethodPlaybookUtil, "simpleExecutor", simpleExecutor);
    }

    @Test
    void givenAGetOperationImplemented_whenCallingTheHttpMethodsPlaybook_thenResultsAreCorrectlyReported() {
        PathItem item = new PathItem();
        item.setGet(new Operation());
        item.setPost(new Operation());
        item.setTrace(new Operation());
        item.setPatch(new Operation());
        item.setDelete(new Operation());
        item.setHead(new Operation());
        item.setPut(new Operation());
        PlaybookData data = PlaybookData.builder().pathItem(item).build();
        httpMethodsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void givenAnOperation_whenCallingTheHttpMethodsPlaybook_thenResultsAreCorrectlyReported() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder()
                .body("{}")
                .responseCode(405)
                .httpMethod("POST")
                .headers(List.of(new KeyValuePair<>("Allow", "GET, PUT, DELETE")))
                .build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        httpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(7)).reportResultInfo(Mockito.any(), Mockito.any(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405}));
    }

    @Test
    void givenAnOperation_whenCallingTheHttpMethodsPlaybookAndTheServiceResponsesWithA2xx_thenResultsAreCorrectlyReported() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).httpMethod("POST").build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        httpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(7)).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405, 200}));
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(httpMethodsPlaybook).hasToString(httpMethodsPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(httpMethodsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldNotSkipForAnyHttpMethod() {
        Assertions.assertThat(httpMethodsPlaybook.skipForHttpMethods()).isEmpty();
    }

    @Test
    void shouldNotRunSamePathTwice() {
        PlaybookData data = PlaybookData.builder().path("/pet").pathItem(new PathItem()).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).httpMethod("POST").build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        httpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(7)).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405, 200}));
        httpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(7)).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405, 200}));
    }

    @Test
    void givenAnOperation_whenCallingTheHttpMethodsPlaybookAndTheServiceResponsesWithNon2xxNon405_thenWarningIsReported() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(500).httpMethod("POST").build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        httpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(7)).reportResultWarn(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405, 500}));
    }

    @Test
    void givenAnOperation_whenCallingTheHttpMethodsPlaybookAndTheServiceResponsesWith405ButMissingAllowHeader_thenWarningIsReported() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder()
                .body("{}")
                .responseCode(405)
                .httpMethod("POST")
                .headers(List.of())
                .build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        httpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(7)).reportResultWarn(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq("POST"), AdditionalMatchers.aryEq(new Object[]{405}));
    }

    @Test
    void givenAnOperation_whenCallingTheHttpMethodsPlaybookAndTheServiceResponsesWith405AndAllowHeaderContainsMethod_thenWarningIsReported() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder()
                .body("{}")
                .responseCode(405)
                .httpMethod("POST")
                .headers(List.of(new KeyValuePair<>("Allow", "GET, POST, PUT")))
                .build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        httpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(7)).reportResultWarn(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.eq("POST"), AdditionalMatchers.aryEq(new Object[]{405, "POST"}));
    }

    @Test
    void givenAnOperation_whenCallingTheHttpMethodsPlaybookAndTheServiceResponsesWith405AndValidAllowHeader_thenInfoIsReported() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder()
                .body("{}")
                .responseCode(405)
                .httpMethod("POST")
                .headers(List.of(new KeyValuePair<>("Allow", "GET, PUT, DELETE")))
                .build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        httpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(7)).reportResultInfo(Mockito.any(), Mockito.any(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405}));
    }
}
