package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

@QuarkusTest
class InsertWhitespacesInFieldNamesFieldPlaybookTest {
    @InjectSpy
    private TestCaseListener testCaseListener;
    private ServiceCaller serviceCaller;

    private InsertWhitespacesInFieldNamesFieldPlaybook insertWhitespacesInFieldNamesFieldPlaybook;

    private HttpResponse httpResponse;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        insertWhitespacesInFieldNamesFieldPlaybook = new InsertWhitespacesInFieldNamesFieldPlaybook(serviceCaller, testCaseListener);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void shouldNotRunForEmptyPayload() {
        insertWhitespacesInFieldNamesFieldPlaybook.run(Mockito.mock(PlaybookData.class));

        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldNotRunForFieldNotInPayload() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getPayload()).thenReturn("{\"field1\": \"value1\"}");
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("field2"));
        insertWhitespacesInFieldNamesFieldPlaybook.run(data);

        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldRunWhenFieldInPayload() {
        httpResponse = HttpResponse.builder().body("{}").responseCode(200).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getPayload()).thenReturn("{\"field1\": \"value1\"}");
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("field1"));
        insertWhitespacesInFieldNamesFieldPlaybook.run(data);

        Mockito.verify(testCaseListener).reportResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX));
    }

    @Test
    void shouldHaveProperDescriptionAndToString() {
        Assertions.assertThat(insertWhitespacesInFieldNamesFieldPlaybook.description()).isNotNull();
        Assertions.assertThat(insertWhitespacesInFieldNamesFieldPlaybook).hasToString(insertWhitespacesInFieldNamesFieldPlaybook.getClass().getSimpleName());
    }
}
