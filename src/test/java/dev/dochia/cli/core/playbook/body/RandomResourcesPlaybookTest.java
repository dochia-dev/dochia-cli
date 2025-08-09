package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.argThat;

@QuarkusTest
class RandomResourcesPlaybookTest {

    private ServiceCaller serviceCaller;
    private FilesArguments filesArguments;
    @InjectSpy
    private TestCaseListener testCaseListener;

    private RandomResourcesPlaybook randomResourcesPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        SimpleExecutor simpleExecutor = new SimpleExecutor(testCaseListener, serviceCaller);
        randomResourcesPlaybook = new RandomResourcesPlaybook(simpleExecutor, filesArguments, testCaseListener);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void shouldNotRunForPathsWithNoVariables() {
        randomResourcesPlaybook.run(PlaybookData.builder().path("/test").build());

        Mockito.verifyNoInteractions(testCaseListener);
    }

    @ParameterizedTest
    @CsvSource({"GET,10", "POST,20"})
    void shouldNotReplaceUrlParams(HttpMethod method, int times) {
        PlaybookData data = PlaybookData.builder().method(method).path("/test/{id}/another/{urlParam}").
                reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).responseCodes(Set.of("400")).build();
        ReflectionTestUtils.setField(data, "processedPayload", "{\"id\":\"d46df8b7-7d69-4bb4-b63b-88c3ebe0e1b8\"}");
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(404).build();

        Mockito.when(filesArguments.getUrlParam("urlParam")).thenReturn("urlParamValue");
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        randomResourcesPlaybook.run(data);
        Mockito.verify(serviceCaller, Mockito.times(times)).call(argThat(serviceData -> !serviceData.getPayload().contains("urlParamValue")));
        Mockito.verify(testCaseListener, Mockito.times(times)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX_NF_AND_VALIDATION), Mockito.anyBoolean(), Mockito.eq(true));
    }

    @ParameterizedTest
    @CsvSource({"{\"id\":12345}", "{\"id\":\"d46df8b7-7d69-4bb4-b63b-88c3ebe0e1b8\"}", "{\"id\":\"someString\"}"})
    void shouldRunForPathsWithVariables(String payload) {
        PlaybookData data = PlaybookData.builder().method(HttpMethod.GET).path("/test/{id}").
                reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).responseCodes(Set.of("400")).build();
        ReflectionTestUtils.setField(data, "processedPayload", payload);
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(404).build();

        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any(), Mockito.anyBoolean());

        randomResourcesPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(10)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX_NF_AND_VALIDATION), Mockito.anyBoolean(), Mockito.eq(true));
    }

    @Test
    void shouldRecordErrorWhenPathParamNotDeclared() {
        PlaybookData data = PlaybookData.builder().method(HttpMethod.GET).path("/test/{id}").
                reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
        ReflectionTestUtils.setField(data, "processedPayload", "{}");
        testCaseListener.createAndExecuteTest(Mockito.mock(PrettyLogger.class), randomResourcesPlaybook, () -> randomResourcesPlaybook.run(data), data);

        Mockito.verify(testCaseListener, Mockito.times(10)).recordError("OpenAPI spec is missing definition for id");
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(randomResourcesPlaybook).hasToString(randomResourcesPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(randomResourcesPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldSkipForNonHttpBodyMethods() {
        Assertions.assertThat(randomResourcesPlaybook.skipForHttpMethods())
                .containsOnly(HttpMethod.HEAD, HttpMethod.TRACE);
    }

}
