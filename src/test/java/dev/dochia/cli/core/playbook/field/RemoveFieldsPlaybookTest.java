package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkusTest
class RemoveFieldsPlaybookTest
 {
    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private FilterArguments filterArguments;
    private ProcessingArguments processingArguments;
    private RemoveFieldsPlaybook removeFieldsPlaybook;

    private PlaybookData data;
    private HttpResponse httpResponse;

    @BeforeEach
    void setup() {
        filterArguments = Mockito.mock(FilterArguments.class);
        processingArguments = Mockito.mock(ProcessingArguments.class);
        serviceCaller = Mockito.mock(ServiceCaller.class);
        removeFieldsPlaybook = new RemoveFieldsPlaybook(serviceCaller, testCaseListener, filterArguments, processingArguments);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void shouldSkipPlaybookIfSkippedTests() {
        data = Mockito.mock(PlaybookData.class);
        Mockito.when(processingArguments.getFieldsSelectionStrategy()).thenReturn(ProcessingArguments.SetSelectionStrategy.ONEBYONE);
        Mockito.when(data.getAllFields(Mockito.any(), Mockito.anyInt())).thenReturn(Collections.singleton(Collections.singleton("id")));
        Mockito.when(filterArguments.getSkipFields()).thenReturn(Collections.singletonList("id"));
        removeFieldsPlaybook.run(data);

        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void givenARequest_whenApplyingTheRemoveFieldsPlaybook_thensAreCorrectlyExecuted() {
        setup("{\"field\":\"oldValue\"}");
        removeFieldsPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX));
        Mockito.verify(testCaseListener, Mockito.times(2)).skipTest(Mockito.any(), Mockito.eq("Field is from a different ANY_OF or ONE_OF payload"));
    }

    @Test
    void shouldRunPlaybookWhenPayloadIsArray() {
        setup("[{\"field\":\"oldValue\"}, {\"field\":\"newValue\"}]");
        removeFieldsPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX));
        Mockito.verify(testCaseListener, Mockito.times(2)).skipTest(Mockito.any(), Mockito.eq("Field is from a different ANY_OF or ONE_OF payload"));
    }

    @Test
    void givenARemoveFieldsPlaybookInstance_whenCallingTheMethodInheritedFromTheBaseClass_thenTheMethodsAreProperlyOverridden() {
        Assertions.assertThat(removeFieldsPlaybook.description()).isNotNull();
        Assertions.assertThat(removeFieldsPlaybook).hasToString(removeFieldsPlaybook.getClass().getSimpleName());
        Assertions.assertThat(removeFieldsPlaybook.skipForHttpMethods()).containsExactly(HttpMethod.GET, HttpMethod.DELETE);
    }

    private void setup(String payload) {
        httpResponse = HttpResponse.builder().body("{}").responseCode(200).build();
        Map<String, List<String>> responses = new HashMap<>();
        responses.put("200", Collections.singletonList("response"));
        Schema schema = new ObjectSchema();
        schema.setProperties(this.createPropertiesMap());
        schema.setRequired(Collections.singletonList("field"));
        Mockito.when(processingArguments.getFieldsSelectionStrategy()).thenReturn(ProcessingArguments.SetSelectionStrategy.ONEBYONE);
        data = PlaybookData.builder().path("path1").method(HttpMethod.POST).payload(payload).
                responses(responses).reqSchema(schema).schemaMap(this.createPropertiesMap()).responseCodes(Collections.singleton("200"))
                .requestContentTypes(List.of("application/json")).requestPropertyTypes(this.createPropertiesMap()).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);
    }

    private Map<String, Schema> createPropertiesMap() {
        Map<String, Schema> schemaMap = new HashMap<>();
        schemaMap.put("field", new StringSchema());
        schemaMap.put("anotherField#test", new StringSchema());
        schemaMap.put("anotherField", new StringSchema());

        return schemaMap;
    }
}
