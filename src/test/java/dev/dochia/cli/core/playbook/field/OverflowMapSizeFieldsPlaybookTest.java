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
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
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
class OverflowMapSizeFieldsPlaybookTest
 {
    ServiceCaller serviceCaller;
    @InjectSpy
    TestCaseListener testCaseListener;
    FieldsIteratorExecutor executor;
    private OverflowMapSizeFieldsPlaybook overflowMapSizeFieldsPlaybook;

    @Inject
    ProcessingArguments processingArguments;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        executor = new FieldsIteratorExecutor(serviceCaller, testCaseListener, Mockito.mock(MatchArguments.class), Mockito.mock(FilesArguments.class));
        overflowMapSizeFieldsPlaybook = new OverflowMapSizeFieldsPlaybook(executor, processingArguments);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(overflowMapSizeFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(overflowMapSizeFieldsPlaybook).hasToString("OverflowMapSizeFieldsPlaybook");
    }

    @Test
    void shouldSkipForNonBodyMethods() {
        Assertions.assertThat(overflowMapSizeFieldsPlaybook.skipForHttpMethods())
                .contains(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD);
    }

    @Test
    void shouldSkipIfFieldPrimitive() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("primitiveField"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("primitiveField", new Schema()));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"primitiveField": 3}
                """);
        overflowMapSizeFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @ParameterizedTest
    @CsvSource(value = {"20", "null"}, nullValues = "null")
    void shouldRunIfFieldDictionary(Integer maxItems) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Schema mapSchema = new MapSchema().maxProperties(maxItems).additionalProperties(true);
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().body("{}").responseCode(200).build());
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("mapField"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("mapField", mapSchema));
        Mockito.when(data.getPayload()).thenReturn("""
                   {"mapField": {
                            "inner": "innerValue"
                        }
                    }
                """);
        overflowMapSizeFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX));
    }

    @Test
    void shouldSkipWhenMapFieldButNotFound() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Schema mapSchema = new MapSchema().maxProperties(10).additionalProperties(true);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("notFound"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("notFound", mapSchema));
        Mockito.when(data.getPayload()).thenReturn("""
                   {"mapField": {
                            "inner": "innerValue"
                        }
                    }
                """);
        overflowMapSizeFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }


    @Test
    void shouldSkipIfFieldObjectAndNotDictionary() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().body("{}").responseCode(200).build());

        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("objectField"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("objectField", new Schema()));

        Mockito.when(data.getPayload()).thenReturn("""
                    {"objectField": {
                            "inner": "innerValue"
                        }
                    }
                """);
        overflowMapSizeFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }
}
