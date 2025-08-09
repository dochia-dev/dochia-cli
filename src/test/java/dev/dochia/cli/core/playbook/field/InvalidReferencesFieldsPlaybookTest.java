package dev.dochia.cli.core.playbook.field;


import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.http.HttpMethod;
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
class InvalidReferencesFieldsPlaybookTest
 {

    private InvalidReferencesFieldsPlaybook invalidReferencesFieldsPlaybook;
    private FilesArguments filesArguments;
    private SimpleExecutor simpleExecutor;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private ServiceCaller serviceCaller;


    @BeforeEach
    void setup() {
        filesArguments = Mockito.mock(FilesArguments.class);
        simpleExecutor = Mockito.mock(SimpleExecutor.class);
        serviceCaller = Mockito.mock(ServiceCaller.class);
        ReflectionTestUtils.setField(simpleExecutor, "serviceCaller", serviceCaller);
        ReflectionTestUtils.setField(simpleExecutor, "testCaseListener", testCaseListener);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        invalidReferencesFieldsPlaybook = new InvalidReferencesFieldsPlaybook(filesArguments, simpleExecutor, testCaseListener);
    }

    @Test
    void shouldNotSkipForAnyMethod() {
        Assertions.assertThat(invalidReferencesFieldsPlaybook.skipForHttpMethods()).isEmpty();
    }

    @Test
    void shouldNotSkipForAnyField() {
        Assertions.assertThat(invalidReferencesFieldsPlaybook.skipForFields()).isEmpty();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(invalidReferencesFieldsPlaybook.toString()).isNotBlank();
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(invalidReferencesFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldNotRunAnyExecutorWhenMethodWithBodyAndPathNotHaveVariables() {
        PlaybookData data = PlaybookData.builder().path("/test").method(HttpMethod.POST).build();
        invalidReferencesFieldsPlaybook.run(data);

        Mockito.verifyNoInteractions(simpleExecutor);
    }

    @Test
    void shouldRunSimpleExecutorForMethodWithBody() {
        PlaybookData data = PlaybookData.builder().path("/test/{id}/{sub}").method(HttpMethod.POST).build();
        invalidReferencesFieldsPlaybook.run(data);

        Mockito.verify(simpleExecutor, Mockito.times(46)).execute(Mockito.any());
    }

    @Test
    void shouldRunFieldsIteratorExecutorForMethodWithoutBody() {
        PlaybookData data = PlaybookData.builder().path("/test/{test}").method(HttpMethod.GET).build();
        invalidReferencesFieldsPlaybook.run(data);

        Mockito.verify(simpleExecutor, Mockito.times(23)).execute(Mockito.any());
    }

    @Test
    void shouldReportError() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("field"));
        Mockito.when(data.getMethod()).thenReturn(HttpMethod.POST);
        Mockito.when(data.getPath()).thenReturn("/test/{id}/{sub}");
        Mockito.doCallRealMethod().when(simpleExecutor).execute(Mockito.any());
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().responseCode(500).httpMethod("POST").build());
        Mockito.doNothing().when(testCaseListener).reportResultError(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        invalidReferencesFieldsPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(46)).reportResultError(Mockito.any(), Mockito.eq(data),
                Mockito.eq("Unexpected response code: 500"),
                Mockito.eq("Request failed unexpectedly for http method [{}]: expected [{}], actual [{}]"),
                Mockito.eq(new Object[]{"POST", "4XX, 2XX", "500"}));
    }

    @ParameterizedTest
    @CsvSource({"222", "444"})
    void shouldReportInfo(int responseCode) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("field"));
        Mockito.when(data.getMethod()).thenReturn(HttpMethod.POST);
        Mockito.when(data.getPath()).thenReturn("/test/{id}/{sub}");
        Mockito.doCallRealMethod().when(simpleExecutor).execute(Mockito.any());
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().responseCode(responseCode).httpMethod("POST").build());
        Mockito.doNothing().when(testCaseListener).reportResultInfo(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        invalidReferencesFieldsPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(46)).reportResultInfo(Mockito.any(), Mockito.eq(data),
                Mockito.eq("Response code expected: [{}]"),
                Mockito.eq(new Object[]{responseCode}));
    }
}
