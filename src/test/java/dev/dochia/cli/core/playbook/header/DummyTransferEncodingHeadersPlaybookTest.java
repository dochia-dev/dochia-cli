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
class DummyTransferEncodingHeadersPlaybookTest {
    private DummyTransferEncodingHeadersPlaybook dummyTransferEncodingHeadersPlaybook;

    @BeforeEach
    void setup() {
        dummyTransferEncodingHeadersPlaybook = new DummyTransferEncodingHeadersPlaybook(Mockito.mock(SimpleExecutor.class));
    }

    @Test
    void shouldProperlyOverrideParentMethods() {
        Assertions.assertThat(dummyTransferEncodingHeadersPlaybook.typeOfHeader()).isEqualTo("dummy");
        Assertions.assertThat(dummyTransferEncodingHeadersPlaybook.description()).isEqualTo("send a request with a dummy Transfer-Encoding header and expect to get 400|501 code");
        Assertions.assertThat(dummyTransferEncodingHeadersPlaybook).hasToString(dummyTransferEncodingHeadersPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldReturnAValidHeadersList() {
        PlaybookData data = PlaybookData.builder().headers(new HashSet<>(HEADERS))
                .responseContentTypes(Collections.singletonMap("200", Collections.singletonList("application/json"))).build();

        List<Set<DochiaHeader>> headers = dummyTransferEncodingHeadersPlaybook.getHeaders(data);

        Assertions.assertThat(headers).hasSize(1);
        Assertions.assertThat(headers.getFirst()).contains(DochiaHeader.builder().name("Transfer-Encoding").build());
    }

    @Test
    void shouldReturn400501ResponseCode() {
        Assertions.assertThat(dummyTransferEncodingHeadersPlaybook.getResponseCodeFamily()).isEqualTo(ResponseCodeFamilyPredefined.FOUR00_FIVE01);
    }

    @Test
    void shouldNotMatchResponseContentType() {
        Assertions.assertThat(dummyTransferEncodingHeadersPlaybook.shouldMatchContentType()).isFalse();
    }
}