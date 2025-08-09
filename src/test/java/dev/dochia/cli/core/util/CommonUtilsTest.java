package dev.dochia.cli.core.util;

import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.PathItem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@QuarkusTest
class CommonUtilsTest {

    @Test
    void givenASetAndMinSize_whenGettingAllSetsWithMinSize_thenAllSubsetsAreProperlyReturned() {
        Set<String> data = new HashSet<>(Arrays.asList("a", "b", "c"));
        Set<Set<String>> sets = PlaybookData.SetFuzzingStrategy.getAllSetsWithMinSize(data, 2);

        Assertions.assertThat(sets)
                .isNotEmpty()
                .containsExactlyInAnyOrder(Collections.singleton("a"), Collections.singleton("b"), Collections.singleton("c"),
                        new HashSet<>(Arrays.asList("a", "b")), new HashSet<>(Arrays.asList("a", "c")), new HashSet<>(Arrays.asList("b", "c")));

    }

    @ParameterizedTest
    @CsvSource(value = {"{\"field\":\"value\", \"anotherField\":\"otherValue\"}|field",
            "{\"field\": 2, \"anotherField\":\"otherValue\"}|field",
            "[{\"field\": 2, \"anotherField\":\"otherValue\"},{\"field\": 2, \"anotherField\":\"otherValue\"}]|field",
            "{\"field\": {\"subField\":\"value\"}, \"anotherField\":\"otherValue\"}|field#subField",
            "{\"field\": [{\"subField\":\"value\"},{\"subField\":\"value\"}], \"anotherField\":\"otherValue\"}|field[*]#subField"}, delimiter = '|')
    void givenAPayloadAndAFuzzingStrategy_whenReplacingTheFuzzedValue_thenThePayloadIsProperlyFuzzed(String json, String path) {
        FuzzingStrategy strategy = FuzzingStrategy.replace().withData("fuzzed");
        FuzzingResult result = FuzzingStrategy.replaceField(json, path, strategy);

        Assertions.assertThat(result.fuzzedValue()).isEqualTo("fuzzed");
        Assertions.assertThat(result.json()).contains("fuzzed");
    }

    @Test
    void shouldAddTopElement() {
        String payload = "{\"field\":\"value\", \"anotherField\":{\"subfield\": \"otherValue\"}}";

        Map<String, Object> currentPathValues = Collections.singletonMap("additionalProperties", "{topElement=metadata, mapValues={test1=value1,test2=value2}}");
        String updatedPayload = CommonUtils.setAdditionalPropertiesToPayload(currentPathValues, payload);
        Assertions.assertThat(updatedPayload).contains("metadata").contains("test1");
    }

    @Test
    void shouldNotAddTopElement() {
        String payload = "{\"field\":\"value\", \"anotherField\":{\"subfield\": \"otherValue\"}}";

        Map<String, Object> currentPathValues = Collections.singletonMap("additionalProperties", "{mapValues={test1=value1,test2=value2}}");
        String updatedPayload = CommonUtils.setAdditionalPropertiesToPayload(currentPathValues, payload);
        Assertions.assertThat(updatedPayload).doesNotContain("metadata").contains("test1");
    }

    @ParameterizedTest
    @CsvSource({"test,java.lang.String", "10,java.lang.Integer", "20L,java.lang.Long", "20.34,java.lang.Float"})
    void shouldConvertToAppropriateType(String value, String typeName) throws ClassNotFoundException {
        Class<?> expectedType = Class.forName(typeName);
        Object result = CommonUtils.getAsAppropriateType(value);
        Assertions.assertThat(result).isInstanceOf(expectedType);
    }

    @Test
    void shouldReplaceJsonArray() {
        String json = """
                {"key": [3,4,5]}
                """;

        String result = CommonUtils.justReplaceField(json, "key", "replaced").json();
        Assertions.assertThat(result).contains("replaced").doesNotContain("3", "4", "5");
    }

    @Test
    void shouldReplaceArray() {
        String json = """
                ["test1", "test2", "test3"]
                """;
        String result = CommonUtils.justReplaceField(json, "$[*]", "replaced").json();
        Assertions.assertThat(result).contains("replaced").doesNotContain("test1", "test2", "test3");
    }

    @ParameterizedTest
    @CsvSource({"$[*],true", "$,true", "$.field,false", "$.field[*],false", "$.field[0],false", "$.field[1].subField[*],false"})
    void shouldTestIsRootArray(String input, boolean expected) {
        boolean result = CommonUtils.isRootArray(input);
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldReturnFileEmptyWhenFileNull() {
        Assertions.assertThat(CommonUtils.isFileEmpty(null)).isTrue();
    }

    @Test
    void shouldReturnFileEmptyWhenFileEmpty() {
        File file = new File("src/test/resources/empty.yml");
        Assertions.assertThat(CommonUtils.isFileEmpty(file)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"true,strYYing", "false,stYYng"})
    void shouldInsertInTheMiddleWithoutReplace(boolean insertWithoutReplace, String toCheck) {
        String finalString = CommonUtils.insertInTheMiddle("string", "YY", insertWithoutReplace);

        Assertions.assertThat(finalString).isEqualTo(toCheck);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "''; path1,path2; path1",
            "path2; path1,path2; path2",
            "path3; path1,path2,path3; path3",
            "path4,path2; path1,path2,path3; path2"
    }, delimiter = ';')
    void testPathSorting(String pathsOrderString, String inputPathsString, String expectedFirstPath) {
        List<String> pathsOrder = pathsOrderString.isEmpty() ?
                Collections.emptyList() : Arrays.asList(pathsOrderString.split(","));

        Map<String, PathItem> inputMap = Arrays.stream(inputPathsString.split(","))
                .collect(Collectors.toMap(p -> p, p -> new PathItem()));

        Comparator<Map.Entry<String, PathItem>> customComparator =
                CommonUtils.createCustomComparatorBasedOnPathsOrder(pathsOrder);

        LinkedHashSet<Map.Entry<String, PathItem>> sorted = inputMap.entrySet()
                .stream().sorted(customComparator)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Assertions.assertThat(sorted.getFirst().getKey()).isEqualTo(expectedFirstPath);
    }

    @ParameterizedTest
    @CsvSource(value = {"http://localhost:8080/tests/%7Btest%7D|http://localhost:8080/tests/{test}",
            "http://localhost:8080/tests/%7Btest%7D%7Btest2%7D|http://localhost:8080/tests/{test}{test2}",
            "http://localhost:8080/tests/%7Btest%7D%7Btest2%7D%7Btest3%7D|http://localhost:8080/tests/{test}{test2}{test3}",
            "http://localhost:8080/tests/%7Btest%7D%7Btest2%7D%7Btest3%7D%7Btest4%7D|http://localhost:8080/tests/{test}{test2}{test3}{test4}",
            "http://localhost:8080/tests/%7Btest%7D%7Btest2%7D%7Btest3%7D%7Btest4%7D%7Btest5%7D|http://localhost:8080/tests/{test}{test2}{test3}{test4}{test5}",
            "null|null"}, delimiter = '|', nullValues = "null")
    void shouldUnescapeCurlyBrackets(String url, String expected) {
        String result = CommonUtils.unescapeCurlyBrackets(url);
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = {"inputCase", "InputCase", "INPUTCASE", "inputcase", "test9"})
    void shouldRandomizeCase(String input) {
        String result = CommonUtils.randomizeCase(input);
        Assertions.assertThat(result).isNotEqualTo(input).isEqualToIgnoringCase(input);
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''"}, nullValues = "null")
    void shouldCheckNullAndEmpty(String value) {
        String result = CommonUtils.randomizeCase(value);
        Assertions.assertThat(result).isNullOrEmpty();
    }
}
