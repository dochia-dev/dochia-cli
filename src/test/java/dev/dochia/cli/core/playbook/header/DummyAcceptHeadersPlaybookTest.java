package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.dochia.cli.core.playbook.header.UnsupportedAcceptHeadersPlaybookTest.HEADERS;

@QuarkusTest
class DummyAcceptHeadersPlaybookTest {
    private DummyAcceptHeadersPlaybook dummyAcceptHeadersPlaybook;

    @BeforeEach
    void setup() {
        dummyAcceptHeadersPlaybook = new DummyAcceptHeadersPlaybook(Mockito.mock(SimpleExecutor.class));
    }

    @Test
    void shouldProperlyOverrideParentMethods() {
        Assertions.assertThat(dummyAcceptHeadersPlaybook.typeOfHeader()).isEqualTo("dummy");
        Assertions.assertThat(dummyAcceptHeadersPlaybook.description()).isNotNull();
        Assertions.assertThat(dummyAcceptHeadersPlaybook).hasToString(dummyAcceptHeadersPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldReturnAValidHeadersList() {
        PlaybookData data = PlaybookData.builder().headers(new HashSet<>(HEADERS))
                .responseContentTypes(Collections.singletonMap("200", Collections.singletonList("application/json"))).build();

        List<Set<DochiaHeader>> headers = dummyAcceptHeadersPlaybook.getHeaders(data);

        Assertions.assertThat(headers).hasSize(1);
        Assertions.assertThat(headers.getFirst()).contains(DochiaHeader.builder().name("Accept").build());
    }

    @Test
    void shouldReturn4XXMTResponseCode() {
        Assertions.assertThat(dummyAcceptHeadersPlaybook.getResponseCodeFamily()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX_MT);
    }
}
