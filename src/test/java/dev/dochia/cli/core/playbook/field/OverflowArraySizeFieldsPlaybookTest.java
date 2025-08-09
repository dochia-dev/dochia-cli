package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.MatchArguments;
import dev.dochia.cli.core.args.ProcessingArguments;
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
import io.swagger.v3.oas.models.media.ArraySchema;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Set;

@QuarkusTest
class OverflowArraySizeFieldsPlaybookTest
 {
    ServiceCaller serviceCaller;
    @InjectSpy
    TestCaseListener testCaseListener;
    FieldsIteratorExecutor executor;
    private OverflowArraySizeFieldsPlaybook overflowArraySizeFieldsPlaybook;

    @Inject
    ProcessingArguments processingArguments;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        executor = new FieldsIteratorExecutor(serviceCaller, testCaseListener, Mockito.mock(MatchArguments.class), Mockito.mock(FilesArguments.class));
        overflowArraySizeFieldsPlaybook = new OverflowArraySizeFieldsPlaybook(executor, processingArguments);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(overflowArraySizeFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(overflowArraySizeFieldsPlaybook).hasToString("OverflowArraySizeFieldsPlaybook");
    }

    @Test
    void shouldSkipForNonBodyMethods() {
        Assertions.assertThat(overflowArraySizeFieldsPlaybook.skipForHttpMethods())
                .contains(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD);
    }

    @Test
    void shouldSkipIfFieldPrimitive() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("primitiveField"));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"primitiveField": 3}
                """);
        overflowArraySizeFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @ParameterizedTest
    @CsvSource(value = {"20", "null"}, nullValues = "null")
    void shouldRunIfFieldArray(Integer maxItems) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().body("{}").responseCode(200).build());
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("arrayField"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("arrayField", new ArraySchema().maxItems(maxItems)));
        Mockito.when(data.getPayload()).thenReturn("""
                   {"arrayField": [{
                            "inner": "innerValue"
                        }]
                    }
                """);
        overflowArraySizeFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX));
    }


    @Test
    void shouldSkipIfFieldObjectAndNotArray() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().body("{}").responseCode(200).build());

        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("objectField"));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"objectField": {
                            "inner": "innerValue"
                        }
                    }
                """);
        overflowArraySizeFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }
}
