package dev.dochia.cli.core.playbook.stateful;

import dev.dochia.cli.core.context.GlobalContext;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
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

@QuarkusTest
class DeletedResourcesNotAvailablePlaybookTest {

    private DeletedResourcesNotAvailablePlaybook checkDeletedResourcesNotAvailablePlaybook;
    private GlobalContext globalContext;
    @InjectSpy
    private TestCaseListener testCaseListener;
    @InjectSpy
    private SimpleExecutor simpleExecutor;
    private ServiceCaller serviceCaller;

    @BeforeEach
    void setup() {
        globalContext = new GlobalContext();
        checkDeletedResourcesNotAvailablePlaybook = new DeletedResourcesNotAvailablePlaybook(simpleExecutor, globalContext, testCaseListener);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        ReflectionTestUtils.setField(simpleExecutor, "testCaseListener", testCaseListener);
        serviceCaller = Mockito.mock(ServiceCaller.class);
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().responseCode(400).build());
        ReflectionTestUtils.setField(simpleExecutor, "serviceCaller", serviceCaller);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(checkDeletedResourcesNotAvailablePlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(checkDeletedResourcesNotAvailablePlaybook.toString()).isNotBlank();
    }

    @CsvSource({"http://localhost:8080/relative-path,/relative-path", "http://localhost/relative-path,/relative-path", "/relative-path,/relative-path"})
    @ParameterizedTest
    void shouldReturnUrl(String url, String expected) {
        Assertions.assertThat(DeletedResourcesNotAvailablePlaybook.getRelativePath(url)).isEqualTo(expected);
    }

    @Test
    void shouldNotRunWhenNotGetRequest() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getMethod()).thenReturn(HttpMethod.POST);
        checkDeletedResourcesNotAvailablePlaybook.run(data);

        Mockito.verifyNoInteractions(simpleExecutor);
    }

    @Test
    void shouldNotRunWhenNoStoredDeleteRequests() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getMethod()).thenReturn(HttpMethod.GET);
        globalContext.getSuccessfulDeletes().clear();
        checkDeletedResourcesNotAvailablePlaybook.run(data);

        Mockito.verifyNoInteractions(simpleExecutor);
    }

    @ParameterizedTest
    @CsvSource({"400", "404", "410"})
    void shouldRunWhenGetAndStoredDeleteRequests(int respCode) {
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().responseCode(respCode).build());
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getMethod()).thenReturn(HttpMethod.GET);
        globalContext.getSuccessfulDeletes().add("http://localhost/path");
        checkDeletedResourcesNotAvailablePlaybook.run(data);

        Mockito.verify(simpleExecutor, Mockito.times(1)).execute(Mockito.any());
        Assertions.assertThat(globalContext.getSuccessfulDeletes()).isEmpty();
    }

    @Test
    void shouldSkipAllMethodsButNotGet() {
        Assertions.assertThat(checkDeletedResourcesNotAvailablePlaybook.skipForHttpMethods()).containsOnly(HttpMethod.HEAD, HttpMethod.PATCH, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.TRACE);
    }
}
