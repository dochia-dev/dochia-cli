package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.MatchArguments;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
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
class DefaultValuesInFieldsPlaybookTest
 {
    ServiceCaller serviceCaller;
    @InjectSpy
    TestCaseListener testCaseListener;
    private FieldsIteratorExecutor executor;
    private DefaultValuesInFieldsPlaybook defaultValuesInFieldsPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        executor = new FieldsIteratorExecutor(serviceCaller, testCaseListener, Mockito.mock(MatchArguments.class), Mockito.mock(FilesArguments.class));
        defaultValuesInFieldsPlaybook = new DefaultValuesInFieldsPlaybook(executor);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(defaultValuesInFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(defaultValuesInFieldsPlaybook).hasToString("DefaultValuesInFieldsPlaybook");
    }

    @Test
    void shouldSkipForNonBodyMethods() {
        Assertions.assertThat(defaultValuesInFieldsPlaybook.skipForHttpMethods()).isEmpty();
    }

    @Test
    void shouldSkipIfEnum() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("myField"));
        Schema<String> myEnumSchema = new Schema<>();
        myEnumSchema.setEnum(List.of("one", "two"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("myField", myEnumSchema));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"myField": 3}
                """);
        defaultValuesInFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldSkipIfDiscriminator() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("myField"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("myField", new Schema<String>()));
        Mockito.when(testCaseListener.isFieldNotADiscriminator("myField")).thenReturn(false);
        Mockito.when(data.getPayload()).thenReturn("""
                    {"myField": 3}
                """);
        defaultValuesInFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(0)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX));
    }

    @Test
    void shouldExecuteWhenHavingDefaultAndNoDiscriminatorOrEnum() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().body("{}").responseCode(200).build());
        Schema<String> mySchema = new Schema<>();
        mySchema.setDefault("test");
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("objectField#myField", mySchema));
        Mockito.when(testCaseListener.isFieldNotADiscriminator("objectField#myField")).thenReturn(true);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("objectField#myField"));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"objectField": {
                            "myField": "innerValue"
                        }
                    }
                """);
        defaultValuesInFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX));
    }
}
