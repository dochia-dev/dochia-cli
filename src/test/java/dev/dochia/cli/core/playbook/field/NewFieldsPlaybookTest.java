package dev.dochia.cli.core.playbook.field;

import com.google.gson.JsonElement;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.util.JsonUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.dochia.cli.core.util.DSLWords.NEW_FIELD;

@QuarkusTest
class NewFieldsPlaybookTest {
    private SimpleExecutor simpleExecutor;

    private NewFieldsPlaybook newFieldsPlaybook;

    private PlaybookData data;

    @BeforeEach
    void setup() {
        simpleExecutor = Mockito.mock(SimpleExecutor.class);
        newFieldsPlaybook = new NewFieldsPlaybook(simpleExecutor);
    }

    @Test
    void shouldRunForEmptyPayload() {
        newFieldsPlaybook.run(Mockito.mock(PlaybookData.class));

        Mockito.verify(simpleExecutor).execute(Mockito.any());
    }

    @Test
    void shouldHaveProperDescriptionAndToString() {
        Assertions.assertThat(newFieldsPlaybook.description()).isNotNull();
        Assertions.assertThat(newFieldsPlaybook).hasToString(newFieldsPlaybook.getClass().getSimpleName());
    }

    @Test
    void givenAPOSTRequest_whenCallingTheNewFieldsPlaybook_thenTestCasesAreCorrectlyExecutedAndExpectA4XX() {
        setup(HttpMethod.POST);
        newFieldsPlaybook.run(data);

        Mockito.verify(simpleExecutor, Mockito.times(1)).execute(Mockito.any());
    }

    @Test
    void givenAGETRequest_whenCallingTheNewFieldsPlaybook_thenTestCasesAreCorrectlyExecutedAndExpectA2XX() {
        setup(HttpMethod.GET);
        newFieldsPlaybook.run(data);

        Mockito.verify(simpleExecutor, Mockito.times(1)).execute(Mockito.any());
    }

    @Test
    void shouldAddANewFieldToFuzzToSingleElement() {
        setup(HttpMethod.POST);
        String element = newFieldsPlaybook.addNewField(data);

        Assertions.assertThat(element).contains(NEW_FIELD).doesNotContain(NEW_FIELD + "random");
    }

    @Test
    void shouldAddANewFieldToFuzzToArray() {
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

        Mockito.verify(simpleExecutor, Mockito.times(0)).execute(Mockito.any());
    }

    @Test
    void shouldNotRunForPrimitivePayload() {
        String payload = "1";
        data = PlaybookData.builder().payload(payload).reqSchema(new StringSchema()).build();
        newFieldsPlaybook.run(data);

        Mockito.verify(simpleExecutor, Mockito.times(0)).execute(Mockito.any());
    }

    private void setup(HttpMethod method) {
        Map<String, List<String>> responses = new HashMap<>();
        responses.put("200", Collections.singletonList("response"));
        data = PlaybookData.builder().path("path1").method(method).payload("{'field':'oldValue'}").
                responses(responses).responseCodes(Collections.singleton("200")).reqSchema(new StringSchema())
                .requestContentTypes(List.of("application/json")).build();
    }
}
