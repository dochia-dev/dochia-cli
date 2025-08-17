package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import dev.dochia.cli.core.util.KeyValuePair;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.dochia.cli.core.playbook.header.CheckSecurityHeadersPlaybook.SECURITY_HEADERS;
import static dev.dochia.cli.core.playbook.header.UnsupportedAcceptHeadersPlaybookTest.HEADERS;

@QuarkusTest
class CheckSecurityHeadersPlaybookTest {

    private static final List<KeyValuePair<String, String>> SOME_SECURITY_HEADERS = Arrays.asList(new KeyValuePair<>("Cache-Control", "no-store"),
            new KeyValuePair<>("X-Content-Type-Options", "nosniff"));
    private static final List<KeyValuePair<String, String>> MISSING_HEADERS = Arrays.asList(new KeyValuePair<>("X-Frame-Options", "DENY"),
            new KeyValuePair<>("Content-Security-Policy", "frame-ancestors 'none'"));
    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private CheckSecurityHeadersPlaybook checkSecurityHeadersPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        SimpleExecutor simpleExecutor = new SimpleExecutor(testCaseListener, serviceCaller);
        checkSecurityHeadersPlaybook = new CheckSecurityHeadersPlaybook(testCaseListener, simpleExecutor);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(checkSecurityHeadersPlaybook.description()).isNotNull();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(checkSecurityHeadersPlaybook).hasToString(checkSecurityHeadersPlaybook.getClass().getSimpleName());
    }


    @Test
    void shouldReportMissingSecurityHeaders() {
        PlaybookData data = PlaybookData.builder().headers(new HashSet<>(HEADERS))
                .requestContentTypes(Collections.singletonList("application/json")).reqSchema(new StringSchema()).build();
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(),
                Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX));
        Mockito.doNothing().when(testCaseListener).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any());

        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).headers(SOME_SECURITY_HEADERS).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        checkSecurityHeadersPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(),
                Mockito.eq("Missing recommended Security Headers: {}"), AdditionalMatchers.aryEq(new Object[]{MISSING_HEADERS.stream().map(Object::toString).collect(Collectors.toSet())}));
    }

    @Test
    void shouldNotReportMissingSecurityHeaders() {
        PlaybookData data = PlaybookData.builder().headers(new HashSet<>(HEADERS))
                .requestContentTypes(Collections.singletonList("application/json")).reqSchema(new StringSchema()).build();
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(),
                Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX));
        Mockito.doNothing().when(testCaseListener).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any());
        List<KeyValuePair<String, String>> allHeaders = new ArrayList<>(SOME_SECURITY_HEADERS);
        allHeaders.add(new KeyValuePair<>("dummy", "dummy"));

        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).headers(Stream.concat(allHeaders.stream(), MISSING_HEADERS.stream())
                .toList()).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        checkSecurityHeadersPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX));
    }

    @Test
    void shouldNotReportMissingHeadersWhenCSP() {
        PlaybookData data = PlaybookData.builder().headers(new HashSet<>(HEADERS))
                .requestContentTypes(Collections.singletonList("application/json")).reqSchema(new StringSchema()).build();
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(),
                Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX));
        Mockito.doNothing().when(testCaseListener).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any());
        List<KeyValuePair<String, String>> allHeaders = new ArrayList<>(SOME_SECURITY_HEADERS);
        allHeaders.add(new KeyValuePair<>("Content-Security-Policy", "frame-ancestors 'none'"));
        allHeaders.add(new KeyValuePair<>("X-XSS-Protection", "0"));

        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).headers(allHeaders).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        checkSecurityHeadersPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX));
    }

    @Test
    void shouldReportMismatchingXXSSProtection() {
        PlaybookData data = PlaybookData.builder().headers(new HashSet<>(HEADERS))
                .requestContentTypes(Collections.singletonList("application/json")).reqSchema(new StringSchema()).build();
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(),
                Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX));
        Mockito.doNothing().when(testCaseListener).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any());
        List<KeyValuePair<String, String>> allHeaders = new ArrayList<>(SOME_SECURITY_HEADERS);
        allHeaders.add(new KeyValuePair<>("Content-Security-Policy", "frame-ancestors 'none'"));
        allHeaders.add(new KeyValuePair<>("X-XSS-Protection", "02"));

        HttpResponse httpResponse = HttpResponse.builder().body("{}").responseCode(200).headers(allHeaders).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        checkSecurityHeadersPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResultError(Mockito.any(), Mockito.any(), Mockito.anyString(),
                Mockito.eq("Missing recommended Security Headers: {}"), AdditionalMatchers.aryEq(new Object[]{SECURITY_HEADERS.get("X-XSS-Protection").stream().map(Object::toString).collect(Collectors.toSet())}));
    }
}
