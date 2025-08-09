package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.args.FilesArguments;
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
class BypassAuthenticationPlaybookTest {
    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;
    private SimpleExecutor simpleExecutor;
    private BypassAuthenticationPlaybook bypassAuthenticationPlaybook;

    @BeforeEach
    void setup() {
        filesArguments = Mockito.mock(FilesArguments.class);
        serviceCaller = Mockito.mock(ServiceCaller.class);
        simpleExecutor = new SimpleExecutor(testCaseListener, serviceCaller);
        bypassAuthenticationPlaybook = new BypassAuthenticationPlaybook(simpleExecutor, filesArguments);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void givenAPayloadWithoutAuthenticationHeaders_whenApplyingTheBypassAuthenticationPlaybook_thenThePlaybookIsSkipped() {
        PlaybookData data = PlaybookData.builder().headers(Collections.singleton(DochiaHeader.builder().name("header").value("value").build())).reqSchema(new StringSchema()).build();
        bypassAuthenticationPlaybook.run(data);

        Mockito.verifyNoInteractions(testCaseListener);
    }


    @Test
    void givenAPayloadWithAuthenticationHeadersAndCustomHeaders_whenApplyingTheBypassAuthenticationPlaybook_thenThePlaybookRuns() {
        Mockito.when(filesArguments.getHeaders(Mockito.anyString())).thenReturn(createCustomPlaybookFile());

        Map<String, List<String>> responses = new HashMap<>();
        responses.put("200", Collections.singletonList("response"));
        PlaybookData data = PlaybookData.builder().headers(Collections.singleton(DochiaHeader.builder().name("authorization").value("auth").build())).
                responses(responses).path("test1").reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).responseCodes(Set.of("400")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any(), Mockito.anyBoolean());

        bypassAuthenticationPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX_AA), Mockito.anyBoolean(), Mockito.eq(true));
    }

    @Test
    void givenAPayloadWithAuthenticationHeaders_whenApplyingTheBypassAuthenticationPlaybook_thenThePlaybookRuns() {
        Map<String, List<String>> responses = new HashMap<>();
        responses.put("200", Collections.singletonList("response"));
        PlaybookData data = PlaybookData.builder().headers(Collections.singleton(DochiaHeader.builder().name("authorization").value("auth").build())).
                responses(responses).reqSchema(new StringSchema()).requestContentTypes(List.of("application/json")).responseCodes(Set.of("400")).build();
        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any(), Mockito.anyBoolean());

        bypassAuthenticationPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX_AA), Mockito.anyBoolean(), Mockito.eq(true));
    }

    @Test
    void shouldNotSkipForAnyHttpMethod() {
        Assertions.assertThat(bypassAuthenticationPlaybook.skipForHttpMethods()).isEmpty();
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(bypassAuthenticationPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(bypassAuthenticationPlaybook).hasToString(bypassAuthenticationPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldProperlyIdentifyAuthHeadersFromContract() {
        List<DochiaHeader> headers = Arrays.asList(DochiaHeader.builder().name("jwt").build(), DochiaHeader.builder().name("authorization").build(),
                DochiaHeader.builder().name("api-key").build(), DochiaHeader.builder().name("api_key").build(), DochiaHeader.builder().name("dochia").build());
        PlaybookData data = PlaybookData.builder().headers(new HashSet<>(headers)).reqSchema(new StringSchema()).build();

        Set<String> authHeaders = bypassAuthenticationPlaybook.getAuthenticationHeaderProvided(data);
        Assertions.assertThat(authHeaders).containsExactlyInAnyOrder("jwt", "api-key", "authorization", "api_key");
    }

    @Test
    void shouldProperlyIdentifyAuthHeadersFromHeadersFile() {
        Mockito.when(filesArguments.getHeaders(Mockito.any())).thenReturn(createCustomPlaybookFile());
        PlaybookData data = PlaybookData.builder().headers(new HashSet<>()).path("path1").reqSchema(new StringSchema()).build();
        Set<String> authHeaders = bypassAuthenticationPlaybook.getAuthenticationHeaderProvided(data);
        Assertions.assertThat(authHeaders).containsExactlyInAnyOrder("api-key", "authorization", "jwt");
    }

    private Map<String, Object> createCustomPlaybookFile() {
        Map<String, Object> tests = new HashMap<>();
        tests.put("jwt", "v1");
        tests.put("authorization", "200");
        tests.put("dochia", "mumu");
        tests.put("api-key", "secret");


        return tests;
    }
}
