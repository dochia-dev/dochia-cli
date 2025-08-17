package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

@QuarkusTest
class UnsupportedAcceptHeadersPlaybookTest {
    public static final List<DochiaHeader> HEADERS = Arrays.asList(DochiaHeader.builder().name("Cache-Control").value("no-store").build(),
            DochiaHeader.builder().name("X-Content-Type-Options").value("nosniff").build(),
            DochiaHeader.builder().name("X-Frame-Options").value("DENY").build());
    private UnsupportedAcceptHeadersPlaybook unsupportedAcceptHeadersPlaybook;

    @BeforeEach
    void setup() {
        unsupportedAcceptHeadersPlaybook = new UnsupportedAcceptHeadersPlaybook(null);
    }

    @Test
    void shouldProperlyOverrideParentMethods() {
        Assertions.assertThat(unsupportedAcceptHeadersPlaybook.typeOfHeader()).isEqualTo("unsupported");
        Assertions.assertThat(unsupportedAcceptHeadersPlaybook.description()).isNotNull();
        Assertions.assertThat(unsupportedAcceptHeadersPlaybook).hasToString(unsupportedAcceptHeadersPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldReturnAValidHeadersList() {
        PlaybookData data = PlaybookData.builder().headers(new HashSet<>(HEADERS))
                .responseContentTypes(Collections.singletonMap("200", Collections.singletonList("application/json"))).build();

        List<Set<DochiaHeader>> headers = unsupportedAcceptHeadersPlaybook.getHeaders(data);

        Assertions.assertThat(headers).hasSize(29);
        Assertions.assertThat(headers.getFirst()).contains(DochiaHeader.builder().name("Accept").build());
    }

    @Test
    void shouldReturn4XXMTResponseCode() {
        Assertions.assertThat(unsupportedAcceptHeadersPlaybook.getResponseCodeFamily()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX_MT);
    }
}
