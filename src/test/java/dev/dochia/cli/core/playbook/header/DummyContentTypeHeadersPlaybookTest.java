package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.dochia.cli.core.playbook.header.UnsupportedAcceptHeadersPlaybookTest.HEADERS;

@QuarkusTest
class DummyContentTypeHeadersPlaybookTest {

    private DummyContentTypeHeadersPlaybook dummyContentTypeHeadersPlaybook;

    private SimpleExecutor simpleExecutor;

    @BeforeEach
    void setup() {
        simpleExecutor = Mockito.mock(SimpleExecutor.class);
        dummyContentTypeHeadersPlaybook = new DummyContentTypeHeadersPlaybook(simpleExecutor);
    }

    @Test
    void shouldProperlyOverrideParentMethods() {
        Assertions.assertThat(dummyContentTypeHeadersPlaybook.typeOfHeader()).isEqualTo("dummy");
        Assertions.assertThat(dummyContentTypeHeadersPlaybook.description()).isEqualTo("send a request with a dummy Content-Type header and expect to get 415 code");
        Assertions.assertThat(dummyContentTypeHeadersPlaybook).hasToString(dummyContentTypeHeadersPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldReturnAValidHeadersList() {
        PlaybookData data = PlaybookData.builder().headers(new HashSet<>(HEADERS))
                .responseContentTypes(Collections.singletonMap("200", Collections.singletonList("application/json"))).build();
        ReflectionTestUtils.setField(data, "processedPayload", "{\"id\": 1}");

        List<Set<DochiaHeader>> headers = dummyContentTypeHeadersPlaybook.getHeaders(data);

        Assertions.assertThat(headers).hasSize(1);
        Assertions.assertThat(headers.getFirst()).contains(DochiaHeader.builder().name("Content-Type").build());
    }

    @Test
    void shouldReturnHttpMethodsSkipFor() {
        Assertions.assertThat(dummyContentTypeHeadersPlaybook.skipForHttpMethods()).containsOnly(HttpMethod.GET, HttpMethod.DELETE);
    }

    @Test
    void shouldNotRunWithEmptyPayload() {
        dummyContentTypeHeadersPlaybook.run(Mockito.mock(PlaybookData.class));

        Mockito.verifyNoInteractions(simpleExecutor);
    }

    @Test
    void shouldReturn4XXMTResponseCode() {
        Assertions.assertThat(dummyContentTypeHeadersPlaybook.getResponseCodeFamily()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX_MT);
    }
}
