package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import dev.dochia.cli.core.util.JsonUtils;
import com.google.gson.JsonElement;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
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

import static dev.dochia.cli.core.util.DSLWords.NEW_FIELD;

@QuarkusTest
class NewFieldsPlaybookTest
 {
    @InjectSpy
    private TestCaseListener testCaseListener;
    private ServiceCaller serviceCaller;

    private NewFieldsPlaybook newFieldsPlaybook;

    private PlaybookData data;
    private HttpResponse httpResponse;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        newFieldsPlaybook = new NewFieldsPlaybook(serviceCaller, testCaseListener);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void shouldRunForEmptyPayload() {
        newFieldsPlaybook.run(Mockito.mock(PlaybookData.class));

        Mockito.verify(testCaseListener).reportResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void shouldHaveProperDescriptionAndToString() {
        Assertions.assertThat(newFieldsPlaybook.description()).isNotNull();
        Assertions.assertThat(newFieldsPlaybook).hasToString(newFieldsPlaybook.getClass().getSimpleName());
    }

    @Test
    void givenAPOSTRequest_whenCallingTheNewFieldsPlaybook_thensAreCorrectlyExecutedAndExpectA4XX() {
        setup(HttpMethod.POST);
        newFieldsPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX));
    }

    @Test
    void givenAGETRequest_whenCallingTheNewFieldsPlaybook_thensAreCorrectlyExecutedAndExpectA2XX() {
        setup(HttpMethod.GET);
        newFieldsPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX));
    }

    @Test
    void shouldAddANewFieldToRunToSingleElement() {
        setup(HttpMethod.POST);
        String element = newFieldsPlaybook.addNewField(data);

        Assertions.assertThat(element).contains(NEW_FIELD).doesNotContain(NEW_FIELD + "random");
    }

    @Test
    void shouldAddANewFieldToRunToArray() {
        String payload = "[{ 'field': 'value1'}, {'field': 'value2'}]";
        data = PlaybookData.builder().payload(payload).reqSchema(new StringSchema()).build();
        JsonElement element = JsonUtils.parseAsJsonElement(newFieldsPlaybook.addNewField(data));

        Assertions.assertThat(element.getAsJsonArray().get(0).getAsJsonObject().get(NEW_FIELD)).isNotNull();
        Assertions.assertThat(element.getAsJsonArray().get(0).getAsJsonObject().get(NEW_FIELD + "random")).isNull();
    }

    @Test
    void shouldAddNewFieldWhenPayloadEmpty() {
        data = PlaybookData.builder().path("{}").reqSchema(new StringSchema()).build();
        String fuzzedJson = newFieldsPlaybook.addNewField(data);

        Assertions.assertThat(fuzzedJson).isEqualTo("{\"dochiaFuzzyField\":\"dochiaFuzzyField\"}");
    }

    @Test
    void shouldNotRunWhenPayloadIsArrayOfPrimitives() {
        String payload = "[1, 2, 3]";
        data = PlaybookData.builder().payload(payload).reqSchema(new StringSchema()).build();
        newFieldsPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(0)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX));
    }

    @Test
    void shouldNotRunForPrimitivePayload() {
        String payload = "1";
        data = PlaybookData.builder().payload(payload).reqSchema(new StringSchema()).build();
        newFieldsPlaybook.run(data);

        Mockito.verify(testCaseListener, Mockito.times(0)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX));
    }

    private void setup(HttpMethod method) {
        httpResponse = HttpResponse.builder().body("{}").responseCode(200).build();
        Map<String, List<String>> responses = new HashMap<>();
        responses.put("200", Collections.singletonList("response"));
        data = PlaybookData.builder().path("path1").method(method).payload("{'field':'oldValue'}").
                responses(responses).responseCodes(Collections.singleton("200")).reqSchema(new StringSchema())
                .requestContentTypes(List.of("application/json")).build();

        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);
    }
}
