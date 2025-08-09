package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.MatchArguments;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.http.HttpMethod;
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
class ReplacePrimitivesWithObjectsFieldsPlaybookTest
 {

    ServiceCaller serviceCaller;
    @InjectSpy
    TestCaseListener testCaseListener;
    FieldsIteratorExecutor executor;
    private ReplacePrimitivesWithObjectsFieldsPlaybook replacePrimitivesWithObjectsFieldsPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        executor = new FieldsIteratorExecutor(serviceCaller, testCaseListener, Mockito.mock(MatchArguments.class), Mockito.mock(FilesArguments.class));
        replacePrimitivesWithObjectsFieldsPlaybook = new ReplacePrimitivesWithObjectsFieldsPlaybook(executor);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(replacePrimitivesWithObjectsFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(replacePrimitivesWithObjectsFieldsPlaybook).hasToString("ReplacePrimitivesWithObjectsFieldsPlaybook");
    }

    @Test
    void shouldSkipForNonBodyMethods() {
        Assertions.assertThat(replacePrimitivesWithObjectsFieldsPlaybook.skipForHttpMethods())
                .contains(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD);
    }

    @Test
    void shouldSkipIfFieldObject() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("objectField"));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"objectField": {"inner": "inner"}}
                """);
        replacePrimitivesWithObjectsFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldReplaceIfFieldPrimitive() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().body("{}").responseCode(200).build());

        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("objectField"));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"objectField": 12}
                """);
        replacePrimitivesWithObjectsFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX));
    }
}
