package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkusTest
class DuplicateKeysFieldsPlaybookTest
 {

    @InjectSpy
    private TestCaseListener testCaseListener;
    private ServiceCaller serviceCaller;
    private DuplicateKeysFieldsPlaybook duplicateKeysFieldsPlaybook;
    private PlaybookData data;
    private HttpResponse httpResponse;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        duplicateKeysFieldsPlaybook = new DuplicateKeysFieldsPlaybook(serviceCaller, testCaseListener);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void shouldSkipForEmptyPayload() {
        PlaybookData emptyPayloadData = Mockito.mock(PlaybookData.class);
        Mockito.when(emptyPayloadData.getPayload()).thenReturn("");

        duplicateKeysFieldsPlaybook.run(emptyPayloadData);

        Mockito.verify(testCaseListener, Mockito.times(0)).reportResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldSkipFieldExceedingDepthLimit() {
        setup(HttpMethod.POST);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Collections.singleton("field#nested#too#deep"));

        duplicateKeysFieldsPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(0)).reportResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldSkipFieldNotInPayload() {
        setup(HttpMethod.POST);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Collections.singleton("nonexistentField"));

        duplicateKeysFieldsPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(0)).reportResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldExecuteFieldDuplication() {
        setup(HttpMethod.POST);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Collections.singleton("field"));

        duplicateKeysFieldsPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX));
    }

    @Test
    void shouldHandleErrorDuringPayloadDuplication() {
        setup(HttpMethod.POST);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Collections.singleton("field"));
        Mockito.when(data.getPayload()).thenReturn("invalidJson");

        duplicateKeysFieldsPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(0)).reportResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    private void setup(HttpMethod method) {
        httpResponse = HttpResponse.builder().body("{}").responseCode(200).build();
        Map<String, List<String>> responses = new HashMap<>();
        responses.put("200", Collections.singletonList("response"));
        data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getPath()).thenReturn("path1");
        Mockito.when(data.getMethod()).thenReturn(method);
        Mockito.when(data.getPayload()).thenReturn("{\"field\":\"value\"}");
        Mockito.when(data.getResponses()).thenReturn(responses);
        Mockito.when(data.getResponseCodes()).thenReturn(Collections.singleton("200"));

        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);
    }
}
