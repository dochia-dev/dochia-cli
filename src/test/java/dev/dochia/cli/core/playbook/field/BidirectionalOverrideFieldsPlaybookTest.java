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

import java.util.List;
import java.util.Map;
import java.util.Set;

@QuarkusTest
class BidirectionalOverrideFieldsPlaybookTest
 {
    @InjectSpy
    private TestCaseListener testCaseListener;
    private FieldsIteratorExecutor executor;
    private BidirectionalOverrideFieldsPlaybook bidirectionalOverrideFieldsPlaybook;

    @BeforeEach
    void setup() {
        executor = new FieldsIteratorExecutor(Mockito.mock(ServiceCaller.class), testCaseListener, Mockito.mock(MatchArguments.class), Mockito.mock(FilesArguments.class));
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        bidirectionalOverrideFieldsPlaybook = new BidirectionalOverrideFieldsPlaybook(executor, Mockito.mock(ProcessingArguments.class));
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(bidirectionalOverrideFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(bidirectionalOverrideFieldsPlaybook).hasToString("BidirectionalOverrideFieldsPlaybook");
    }

    @Test
    void shouldSkipForNonBodyMethods() {
        Assertions.assertThat(bidirectionalOverrideFieldsPlaybook.skipForHttpMethods())
                .contains(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD);
    }

    @Test
    void shouldSkipIfFieldNotString() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("nonStringField"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("nonStringField", new Schema()));

        Mockito.when(data.getPayload()).thenReturn("""
                    {"nonStringField": "value"}
                """);
        bidirectionalOverrideFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldSkipIfFieldEnum() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("nonStringField"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("nonStringField",
                new Schema().type("string")._enum(List.of("value1", "value2"))));

        Mockito.when(data.getPayload()).thenReturn("""
                    {"nonStringField": "value"}
                """);
        bidirectionalOverrideFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldSkipIfFieldNotInJson() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("nonStringField"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("nonStringField", new Schema().type("string")));

        Mockito.when(data.getPayload()).thenReturn("""
                    {"anotherFieldHere": "value"}
                """);
        bidirectionalOverrideFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldRunIfFieldString() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("stringField"));

        Schema<String> schema = new Schema<>();
        schema.setType("string");
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("stringField", schema));

        Mockito.when(data.getPayload()).thenReturn("""
                   {"stringField": "originalValue"}
                """);

        bidirectionalOverrideFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(3)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any());
    }
}
