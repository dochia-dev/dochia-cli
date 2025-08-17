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
import java.util.List;
import java.util.Set;


@QuarkusTest
class AcceptLanguageHeadersPlaybookTest {
    private AcceptLanguageHeadersPlaybook acceptLanguageHeadersPlaybook;

    @BeforeEach
    void setup() {
        acceptLanguageHeadersPlaybook = new AcceptLanguageHeadersPlaybook(Mockito.mock(SimpleExecutor.class));
    }

    @Test
    void shouldProperlyOverrideParentMethods() {
        Assertions.assertThat(acceptLanguageHeadersPlaybook.typeOfHeader()).isEqualTo("locale");
        Assertions.assertThat(acceptLanguageHeadersPlaybook.targetHeaderName()).isEqualToIgnoringCase("Accept-Language");
        Assertions.assertThat(acceptLanguageHeadersPlaybook.getExpectedResponseCode()).isEqualTo("200");
        Assertions.assertThat(acceptLanguageHeadersPlaybook.description()).isNotNull();
        Assertions.assertThat(acceptLanguageHeadersPlaybook).hasToString(acceptLanguageHeadersPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldReturnAValidHeadersList() {
        PlaybookData data = PlaybookData.builder().headers(Set.of())
                .responseContentTypes(Collections.singletonMap("200", Collections.singletonList("application/json"))).build();

        List<Set<DochiaHeader>> headers = acceptLanguageHeadersPlaybook.getHeaders(data);

        Assertions.assertThat(headers).hasSize(5);
        Assertions.assertThat(headers.getFirst()).contains(DochiaHeader.builder().name("Accept-Language").build());
    }

    @Test
    void shouldReturnTwoXXResponseCode() {
        Assertions.assertThat(acceptLanguageHeadersPlaybook.getResponseCodeFamily()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
    }
}
