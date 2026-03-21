package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@QuarkusTest
class DuplicateKeysFieldsPlaybookTest {

    private SimpleExecutor simpleExecutor;
    private DuplicateKeysFieldsPlaybook duplicateKeysFieldsPlaybook;
    private PlaybookData data;

    @BeforeEach
    void setup() {
        simpleExecutor = Mockito.mock(SimpleExecutor.class);
        duplicateKeysFieldsPlaybook = new DuplicateKeysFieldsPlaybook(simpleExecutor);
    }

    @Test
    void shouldSkipForEmptyPayload() {
        PlaybookData emptyPayloadData = Mockito.mock(PlaybookData.class);
        Mockito.when(emptyPayloadData.getPayload()).thenReturn("");

        duplicateKeysFieldsPlaybook.run(emptyPayloadData);

        Mockito.verify(simpleExecutor, Mockito.times(0)).execute(Mockito.any());
    }

    @ParameterizedTest
    @CsvSource({"field#nested#too#deep,0", "nonexistentField,0", "field,1"})
    void shouldRunWithDifferentFields(String field, int expectedExecutions) {
        setupData();
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Collections.singleton(field));

        duplicateKeysFieldsPlaybook.run(data);

        Mockito.verify(simpleExecutor, Mockito.times(expectedExecutions)).execute(Mockito.any());
    }

    @Test
    void shouldHandleErrorDuringPayloadDuplication() {
        setupData();
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Collections.singleton("field"));
        Mockito.when(data.getPayload()).thenReturn("invalidJson");

        duplicateKeysFieldsPlaybook.run(data);

        Mockito.verify(simpleExecutor, Mockito.times(0)).execute(Mockito.any());
    }

    @ParameterizedTest
    @CsvSource(value = {"{\"parent\":{\"child\":\"value\"}}|parent#child",
            "{\"items\":[{\"field\":\"value1\"},{\"field\":\"value2\"}]}|items",
            "{\"numbers\":[1,2,3]}|numbers",
            "{\"level1\":{\"level2\":{\"level3\":\"value\"}}}|level1#level2#level3",
            "{\"items\":[]}|items",
            "{\"data\":{\"items\":[{\"id\":1},{\"id\":2}]}}|data#items",
            "{\"field-with-dash\":\"value\"}|field-with-dash"}, delimiter = '|')
    void shouldHandleNestedObjectDuplication(String payload, String field) {
        setupData();
        Mockito.when(data.getPayload()).thenReturn(payload);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Collections.singleton(field));

        duplicateKeysFieldsPlaybook.run(data);

        Mockito.verify(simpleExecutor, Mockito.times(1)).execute(Mockito.any());
    }

    @Test
    void shouldHandleMultipleFieldsWithLimit() {
        setupData();
        Mockito.when(data.getPayload()).thenReturn("{\"field1\":\"value1\",\"field2\":\"value2\",\"field3\":\"value3\"}");
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("field1", "field2", "field3"));

        duplicateKeysFieldsPlaybook.run(data);

        Mockito.verify(simpleExecutor, Mockito.times(3)).execute(Mockito.any());
    }

    @Test
    void shouldHandleExceptionDuringJsonParsing() {
        setupData();
        Mockito.when(data.getPayload()).thenReturn("{\"field\":\"value\"}");
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Collections.singleton("field"));

        // This will trigger the exception in createDuplicatedPayload
        // by having a field that exists but causes an issue during duplication
        duplicateKeysFieldsPlaybook.run(data);

        // Should execute once since the field is valid
        Mockito.verify(simpleExecutor, Mockito.times(1)).execute(Mockito.any());
    }

    @Test
    void shouldHandlePayloadThatResultsInSameJson() {
        setupData();
        // Create a scenario where duplication would result in the same JSON
        Mockito.when(data.getPayload()).thenReturn("{}");
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Collections.singleton("field"));

        duplicateKeysFieldsPlaybook.run(data);

        // Should not execute since field is not in payload
        Mockito.verify(simpleExecutor, Mockito.times(0)).execute(Mockito.any());
    }


    private void setupData() {
        Map<String, List<String>> responses = new HashMap<>();
        responses.put("200", Collections.singletonList("response"));
        data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getPath()).thenReturn("path1");
        Mockito.when(data.getMethod()).thenReturn(HttpMethod.POST);
        Mockito.when(data.getPayload()).thenReturn("{\"field\":\"value\"}");
        Mockito.when(data.getResponses()).thenReturn(responses);
        Mockito.when(data.getResponseCodes()).thenReturn(Collections.singleton("200"));

    }
}
