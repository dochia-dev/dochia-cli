package dev.dochia.cli.core.playbook.header;


import dev.dochia.cli.core.args.ProcessingArguments;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkusTest
class LargeNumberOfRandomAlphanumericHeadersPlaybookTest {

    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private LargeNumberOfRandomAlphanumericHeadersPlaybook largeNumberOfRandomAlphanumericHeadersPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        SimpleExecutor simpleExecutor = new SimpleExecutor(testCaseListener, serviceCaller);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        ProcessingArguments processingArguments = Mockito.mock(ProcessingArguments.class);
        Mockito.when(processingArguments.getRandomHeadersNumber()).thenReturn(10000);
        largeNumberOfRandomAlphanumericHeadersPlaybook = new LargeNumberOfRandomAlphanumericHeadersPlaybook(simpleExecutor, testCaseListener, processingArguments);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(largeNumberOfRandomAlphanumericHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldReturnRandomHeaderValues() {
        Assertions.assertThat(largeNumberOfRandomAlphanumericHeadersPlaybook.randomHeadersValueFunction().apply(10)).isNotBlank();
    }


    @ParameterizedTest
    @CsvSource({"400", "431", "414"})
    void shouldReportInfo(int code) {
        PlaybookData data = setupSimpleFuzzingData();

        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().responseCode(code).build());
        largeNumberOfRandomAlphanumericHeadersPlaybook.run(data);
        Mockito.doNothing().when(testCaseListener).reportResultInfo(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any());

        Mockito.verify(testCaseListener).reportResultInfo(Mockito.any(), Mockito.any(), Mockito.eq("Request returned as expected for http method [{}] with response code [{}]"), Mockito.any());
    }

    @ParameterizedTest
    @CsvSource({"500", "200", "401"})
    void shouldReportError(int code) {
        PlaybookData data = setupSimpleFuzzingData();

        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().responseCode(code).build());
        largeNumberOfRandomAlphanumericHeadersPlaybook.run(data);
        Mockito.doNothing().when(testCaseListener).reportResultError(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any(), Mockito.any());

        Mockito.verify(testCaseListener).reportResultError(Mockito.any(), Mockito.any(), Mockito.eq("Unexpected response code: " + code),
                Mockito.eq("Request failed unexpectedly for http method [{}]: expected {}, actual [{}]"), Mockito.any());

    }

    private static PlaybookData setupSimpleFuzzingData() {
        Map<String, List<String>> responses = new HashMap<>();
        responses.put("200", Collections.singletonList("response"));
        return PlaybookData.builder().headers(Collections.singleton(DochiaHeader.builder().name("header").value("value").build())).
                responses(responses).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).build();
    }
}
