package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.MatchArguments;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.swagger.v3.oas.models.media.Schema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
class AdditionalPropertiesMassAssignmentFieldsPlaybookTest
 {
    @InjectSpy
    TestCaseListener testCaseListener;
    FieldsIteratorExecutor executor;
    private AdditionalPropertiesMassAssignmentFieldsPlaybook additionalPropertiesMassAssignmentFieldsPlaybook;

    @BeforeEach
    void setup() {
        executor = new FieldsIteratorExecutor(Mockito.mock(ServiceCaller.class), testCaseListener, Mockito.mock(MatchArguments.class), Mockito.mock(FilesArguments.class));
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        additionalPropertiesMassAssignmentFieldsPlaybook = new AdditionalPropertiesMassAssignmentFieldsPlaybook(executor, Mockito.mock(ProcessingArguments.class));
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(additionalPropertiesMassAssignmentFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(additionalPropertiesMassAssignmentFieldsPlaybook).hasToString("AdditionalPropertiesMassAssignmentFieldsPlaybook");
    }

    @Test
    void shouldSkipForNonBodyMethods() {
        Assertions.assertThat(additionalPropertiesMassAssignmentFieldsPlaybook.skipForHttpMethods())
                .contains(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD);
    }

    @Test
    void shouldSkipIfNoAdditionalProperties() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("field", Mockito.mock(Schema.class)));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"field": "value"}
                """);
        additionalPropertiesMassAssignmentFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldRunIfAdditionalPropertiesPresent() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("field"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("field", new Schema().additionalProperties(false)));
        Mockito.when(data.getPayload()).thenReturn("""
                   {"field": {"property" : "value", "additionalProperty": "value"}}
                """);

        additionalPropertiesMassAssignmentFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(any(), eq(data), any(), any());
    }
}
