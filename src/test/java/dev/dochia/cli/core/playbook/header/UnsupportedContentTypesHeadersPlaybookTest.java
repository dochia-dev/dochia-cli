package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.DochiaHeader;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.dochia.cli.core.playbook.header.UnsupportedAcceptHeadersPlaybookTest.HEADERS;

@QuarkusTest
class UnsupportedContentTypesHeadersPlaybookTest {
    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private UnsupportedContentTypesHeadersPlaybook unsupportedContentTypeHeadersPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        SimpleExecutor simpleExecutor = new SimpleExecutor(testCaseListener, serviceCaller);
        unsupportedContentTypeHeadersPlaybook = new UnsupportedContentTypesHeadersPlaybook(simpleExecutor);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void shouldNotRunWithPayloadEmpty() {
        unsupportedContentTypeHeadersPlaybook.run(Mockito.mock(PlaybookData.class));

        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldProperlyOverrideParentMethods() {
        Assertions.assertThat(unsupportedContentTypeHeadersPlaybook.typeOfHeader()).isEqualTo("unsupported");
        Assertions.assertThat(unsupportedContentTypeHeadersPlaybook.description()).isEqualTo("send a request with a unsupported Content-Type header and expect to get 415 code");
        Assertions.assertThat(unsupportedContentTypeHeadersPlaybook).hasToString(unsupportedContentTypeHeadersPlaybook.getClass().getSimpleName());
    }

    @Test
    void shouldReturnAValidHeadersList() {
        PlaybookData data = PlaybookData.builder().headers(new HashSet<>(HEADERS))
                .requestContentTypes(Collections.singletonList("application/json")).build();
        ReflectionTestUtils.setField(data, "processedPayload", "{\"id\": 1}");

        List<Set<DochiaHeader>> headers = unsupportedContentTypeHeadersPlaybook.getHeaders(data);

        Assertions.assertThat(headers).hasSize(29);
        Assertions.assertThat(headers.getFirst()).contains(DochiaHeader.builder().name("Content-Type").build());
    }

    @Test
    void shouldRunPlaybookForEachSetOfHeaders() {
        PlaybookData data = PlaybookData.builder().headers(new HashSet<>(HEADERS))
                .requestContentTypes(Collections.singletonList("application/json")).reqSchema(new StringSchema()).build();
        ReflectionTestUtils.setField(data, "processedPayload", "{\"id\": 1}");

        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(),
                Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX_MT), Mockito.anyBoolean(), Mockito.anyBoolean());
        unsupportedContentTypeHeadersPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(29)).reportResult(Mockito.any(),
                Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX_MT), Mockito.anyBoolean(), Mockito.eq(true));
    }

    @Test
    void shouldReturn4XXMTResponseCode() {
        Assertions.assertThat(unsupportedContentTypeHeadersPlaybook.getResponseCodeFamily()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX_MT);
    }
}
