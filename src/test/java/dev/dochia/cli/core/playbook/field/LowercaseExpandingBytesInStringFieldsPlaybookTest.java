package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.MatchArguments;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.http.ResponseCodeFamilyDynamic;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

@QuarkusTest
class LowercaseExpandingBytesInStringFieldsPlaybookTest
 {
    ServiceCaller serviceCaller;
    @InjectSpy
    TestCaseListener testCaseListener;
    FieldsIteratorExecutor executor;
    LowercaseExpandingBytesInStringFieldsPlaybook lowercaseExpandingBytesInStringFieldsPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        executor = new FieldsIteratorExecutor(serviceCaller, testCaseListener, Mockito.mock(MatchArguments.class), Mockito.mock(FilesArguments.class));
        lowercaseExpandingBytesInStringFieldsPlaybook = new LowercaseExpandingBytesInStringFieldsPlaybook(testCaseListener, executor);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(lowercaseExpandingBytesInStringFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(lowercaseExpandingBytesInStringFieldsPlaybook).hasToString("LowercaseExpandingBytesInStringFieldsPlaybook");
    }

    @Test
    void shouldSkipForNonBodyMethods() {
        Assertions.assertThat(lowercaseExpandingBytesInStringFieldsPlaybook.skipForHttpMethods()).isEmpty();
    }

    @Test
    void shouldSkipIfNotStringSchema() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("myField"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("myField", new Schema<>().type("integer")));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"myField": 3}
                """);
        lowercaseExpandingBytesInStringFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldSkipIfFieldNotInPayload() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("myFieldNotInPayload"));
        Schema<String> myStringSchema = new Schema<>().type("string");
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("myFieldNotInPayload", myStringSchema));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"myField": 3}
                """);
        lowercaseExpandingBytesInStringFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(0)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX));
    }

    @ParameterizedTest
    @CsvSource(value = {"5", "null"}, nullValues = "null")
    void shouldReplaceIfStringSchemaAndFieldInPayload(Integer maxLength) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().body("{}").responseCode(200).build());
        Schema<String> myStringSchema = new Schema<>().type("string").maxLength(maxLength);
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("objectField#myField", myStringSchema));
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("objectField#myField"));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"objectField": {
                            "myField": "innerValue"
                        }
                    }
                """);
        lowercaseExpandingBytesInStringFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.eq(new ResponseCodeFamilyDynamic(List.of("2XX", "4XX"))));
    }
}
