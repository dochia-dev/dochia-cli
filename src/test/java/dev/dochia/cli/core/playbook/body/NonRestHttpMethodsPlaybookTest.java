package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
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
class NonRestHttpMethodsPlaybookTest {
    @Inject
    HttpMethodPlaybookUtil httpMethodPlaybookUtil;
    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private NonRestHttpMethodsPlaybook nonRestHttpMethodsPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        nonRestHttpMethodsPlaybook = new NonRestHttpMethodsPlaybook(httpMethodPlaybookUtil);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        SimpleExecutor simpleExecutor = new SimpleExecutor(testCaseListener, serviceCaller);
        ReflectionTestUtils.setField(httpMethodPlaybookUtil, "simpleExecutor", simpleExecutor);
    }

    @Test
    void shouldCallServiceAndReportErrorWhenServiceRespondsWith200() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).httpMethod("POST").build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        nonRestHttpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(17)).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405, 200}));
    }

    @Test
    void shouldCallServiceAndReportInfoWhenServiceRespondsWith405() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(405).httpMethod("POST").build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        nonRestHttpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(17)).reportResultInfo(Mockito.any(), Mockito.any(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405}));
    }

    @Test
    void shouldCallServiceAndReportWarnWhenServiceRespondsWith400() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(400).httpMethod("POST").build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        nonRestHttpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(17)).reportResultWarn(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405, 400}));
    }

    @Test
    void shouldRunOncePerPath() {
        PlaybookData data = PlaybookData.builder().pathItem(new PathItem()).reqSchema(new StringSchema()).path("/test").requestContentTypes(List.of("application/json")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(405).httpMethod("POST").build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        nonRestHttpMethodsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(17)).reportResultInfo(Mockito.any(), Mockito.any(), Mockito.anyString(), AdditionalMatchers.aryEq(new Object[]{"POST", 405}));
        Mockito.clearInvocations(testCaseListener);
        nonRestHttpMethodsPlaybook.run(data);
        Mockito.verifyNoMoreInteractions(testCaseListener);
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(nonRestHttpMethodsPlaybook).hasToString(nonRestHttpMethodsPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldNotSkipForAnyHttpMethod() {
        Assertions.assertThat(nonRestHttpMethodsPlaybook.skipForHttpMethods()).isEmpty();
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(nonRestHttpMethodsPlaybook.description()).isNotBlank();
    }
}
