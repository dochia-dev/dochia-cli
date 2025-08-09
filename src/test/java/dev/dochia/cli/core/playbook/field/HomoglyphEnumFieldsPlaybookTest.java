package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.MatchArguments;
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
class HomoglyphEnumFieldsPlaybookTest
 {
    @InjectSpy
    TestCaseListener testCaseListener;
    FieldsIteratorExecutor executor;
    private HomoglyphEnumFieldsPlaybook homoglyphEnumFieldsPlaybook;

    @BeforeEach
    void setup() {
        executor = new FieldsIteratorExecutor(Mockito.mock(ServiceCaller.class), testCaseListener, Mockito.mock(MatchArguments.class), Mockito.mock(FilesArguments.class));
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        homoglyphEnumFieldsPlaybook = new HomoglyphEnumFieldsPlaybook(executor);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(homoglyphEnumFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(homoglyphEnumFieldsPlaybook).hasToString("HomoglyphEnumFieldsPlaybook");
    }

    @Test
    void shouldSkipForNonBodyMethods() {
        Assertions.assertThat(homoglyphEnumFieldsPlaybook.skipForHttpMethods())
                .contains(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD);
    }

    @Test
    void shouldSkipIfFieldNotEnum() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("nonEnumField"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("nonEnumField", new Schema()));

        Mockito.when(data.getPayload()).thenReturn("""
                    {"nonEnumField": "value"}
                """);
        homoglyphEnumFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldSkipIfFieldNotInJson() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("nonEnumField"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("nonEnumField", new Schema()._enum(List.of("value"))));

        Mockito.when(data.getPayload()).thenReturn("""
                    {"anotherFieldHere": "value"}
                """);
        homoglyphEnumFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldRunIfFieldEnum() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("enumField"));

        Schema<String> schema = new Schema<>();
        schema.setEnum(List.of("originalValue", "mutatedValue"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("enumField", schema));

        Mockito.when(data.getPayload()).thenReturn("""
                   {"enumField": "originalValue"}
                """);

        homoglyphEnumFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(6)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any());
    }
}
