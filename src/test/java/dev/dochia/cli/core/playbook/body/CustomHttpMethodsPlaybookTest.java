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
class CustomHttpMethodsPlaybookTest {
    @Inject
    HttpMethodPlaybookUtil httpMethodPlaybookUtil;
    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private CustomHttpMethodsPlaybook customHttpMethodsPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        customHttpMethodsPlaybook = new CustomHttpMethodsPlaybook(httpMethodPlaybookUtil);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        SimpleExecutor simpleExecutor = new SimpleExecutor(testCaseListener, serviceCaller);
        ReflectionTestUtils.setField(httpMethodPlaybookUtil, "simpleExecutor", simpleExecutor);
    }

    @Test
    void shouldCallServiceAndReportErrorWhenServiceRespondsWith200() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).httpMethod("POST").build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        customHttpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(12)).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405, 200}));
    }

    @Test
    void shouldCallServiceAndReportInfoWhenServiceRespondsWith405() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(405).httpMethod("POST").headers(List.of(new KeyValuePair<>("Allow","GET"))).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        customHttpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(12)).reportResultInfo(Mockito.any(), Mockito.any(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405}));
    }

    @Test
    void shouldCallServiceAndReportWarnWhenServiceRespondsWith400() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(400).httpMethod("POST").build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        customHttpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(12)).reportResultWarn(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405, 400}));
    }

    @Test
    void shouldRunOncePerPath() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).path("/test").requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(405).headers(List.of(new KeyValuePair<>("Allow","GET"))).httpMethod("POST").build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        customHttpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(12)).reportResultInfo(Mockito.any(), Mockito.any(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405}));
        Mockito.clearInvocations(testCaseListener);
        customHttpMethodsPlaybook.run(data);
        Mockito.verifyNoMoreInteractions(testCaseListener);
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(customHttpMethodsPlaybook).hasToString(customHttpMethodsPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldNotSkipForAnyHttpMethod() {
        Assertions.assertThat(customHttpMethodsPlaybook.skipForHttpMethods()).isEmpty();
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(customHttpMethodsPlaybook.description()).isNotBlank();
    }
}
