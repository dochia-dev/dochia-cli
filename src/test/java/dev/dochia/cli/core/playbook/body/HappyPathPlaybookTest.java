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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkusTest
class HappyPathPlaybookTest {
    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;

    private HappyPathPlaybook happyPlaybook;

    private SimpleExecutor simpleExecutor;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        simpleExecutor = new SimpleExecutor(testCaseListener, serviceCaller);
        happyPlaybook = new HappyPathPlaybook(simpleExecutor);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void givenARequest_whenCallingTheHappyPlaybook_thensAreCorrectlyExecuted() {
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).build();
        Map<String, List<String>> responses = new HashMap<>();
        responses.put("200", Collections.singletonList("response"));
        PlaybookData data = PlaybookData.builder().path("path1").method(HttpMethod.POST).payload("{'field':'oldValue'}").
                responses(responses).responseCodes(Collections.singleton("200")).reqSchema(new StringSchema())
                .requestContentTypes(List.of("application/json")).build();

        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        happyPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX), Mockito.anyBoolean(), Mockito.eq(true));
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(happyPlaybook).hasToString(happyPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(happyPlaybook.description()).isNotBlank();
    }

    @Test
    void givenARequest_whenCallingTheHappyPlaybookAndAnErrorOccurs_thensAreCorrectlyReported() {
        PlaybookData data = PlaybookData.builder().path("path1").method(HttpMethod.POST).payload("{'field':'oldValue'}")
                .reqSchema(new StringSchema()).responseCodes(Collections.singleton("200"))
                .requestContentTypes(List.of("application/json")).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenThrow(new RuntimeException("this is deliberately thrown for testing"));

        happyPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any());
    }
}
