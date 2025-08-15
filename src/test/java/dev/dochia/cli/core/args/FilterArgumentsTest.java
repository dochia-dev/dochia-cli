package dev.dochia.cli.core.args;

import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.body.HappyPathPlaybook;
import dev.dochia.cli.core.playbook.stateful.DeletedResourcesNotAvailablePlaybook;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@QuarkusTest
class FilterArgumentsTest {

    @Inject
    FilterArguments filterArguments;

    IgnoreArguments ignoreArguments;

    CheckArguments checkArguments;

    ProcessingArguments processingArguments;

    @BeforeEach
    void setup() {
        checkArguments = new CheckArguments();
        ignoreArguments = new IgnoreArguments();
        processingArguments = new ProcessingArguments();
        ReflectionTestUtils.setField(filterArguments, "checkArguments", checkArguments);
        ReflectionTestUtils.setField(filterArguments, "processingArguments", processingArguments);
        ReflectionTestUtils.setField(filterArguments, "skipPlaybooks", Collections.emptyList());
        ReflectionTestUtils.setField(filterArguments, "suppliedPlaybooks", Collections.emptyList());
        ReflectionTestUtils.setField(filterArguments, "skipFields", Collections.emptyList());
        ReflectionTestUtils.setField(filterArguments, "paths", Collections.emptyList());
        ReflectionTestUtils.setField(filterArguments, "playbooksToBeRunComputed", false);

        FilterArguments.ALL_TEST_CASE_PLAYBOOKS.clear();
        FilterArguments.PLAYBOOKS_TO_BE_RUN.clear();
        FilterArguments.PATHS_TO_INCLUDE.clear();
    }

    @ParameterizedTest
    @CsvSource({"checkHeaders,CheckSecurityHeadersPlaybook,RemoveFieldsPlaybook",
            "checkFields,RemoveFieldsPlaybook,CheckSecurityHeadersPlaybook",
            "checkHttp,HappyPathPlaybook,CheckSecurityHeadersPlaybook",
            "includeEmojis,LeadingMultiCodePointEmojisInFieldsTrimValidatePlaybook,LeadingControlCharsInHeadersPlaybook"})
    void shouldReturnCheckHeadersPlaybooks(String argument, String matching, String notMatching) {
        ReflectionTestUtils.setField(checkArguments, argument, true);
        List<String> playbooks = filterArguments.getFirstPhasePlaybooksForPath();

        Assertions.assertThat(playbooks).contains(matching).doesNotContain(notMatching);
    }

    @Test
    void shouldIncludeAllPlaybooks() {
        ReflectionTestUtils.setField(checkArguments, "includeEmojis", true);
        List<String> playbooks = filterArguments.getFirstPhasePlaybooksForPath();

        Assertions.assertThat(playbooks).contains("LeadingMultiCodePointEmojisInFieldsTrimValidatePlaybook"
                , "RemoveFieldsPlaybook", "CheckSecurityHeadersPlaybook").hasSize(123);
    }

    @Test
    void shouldReturnAllPlaybooksWhenNoCheckSupplied() {
        List<String> playbooks = filterArguments.getFirstPhasePlaybooksForPath();

        Assertions.assertThat(playbooks).contains("CheckSecurityHeadersPlaybook", "HappyPathPlaybook", "RemoveFieldsPlaybook");
    }

    @Test
    void shouldNotReturnContractPlaybooksWhenIgnoredSupplied() {
        ReflectionTestUtils.setField(ignoreArguments, "ignoreResponseCodes", List.of("2xx"));

        List<String> playbooks = filterArguments.getFirstPhasePlaybooksForPath();

        Assertions.assertThat(playbooks).contains("CheckSecurityHeadersPlaybook", "HappyPathPlaybook", "RemoveFieldsPlaybook")
                .doesNotContain("TopLevelElementsLinter");
    }

    @Test
    void shouldNotReturnContractPlaybooksWhenBlackbox() {
        ignoreArguments.setBlackbox(true);
        List<String> playbooks = filterArguments.getFirstPhasePlaybooksForPath();

        Assertions.assertThat(playbooks).contains("CheckSecurityHeadersPlaybook", "HappyPathPlaybook", "RemoveFieldsPlaybook")
                .doesNotContain("TopLevelElementsLinter");
    }

    @Test
    void shouldRemoveSkippedPlaybooks() {
        ReflectionTestUtils.setField(filterArguments, "skipPlaybooks", List.of("VeryLarge", "SecurityHeaders", "Jumbo"));
        List<String> playbooks = filterArguments.getFirstPhasePlaybooksForPath();

        Assertions.assertThat(playbooks).contains("HappyPathPlaybook", "RemoveFieldsPlaybook")
                .doesNotContain("CheckSecurityHeadersPlaybook", "VeryLargeStringsInFieldsPlaybook", "Jumbo");

    }

    @Test
    void shouldOnlyIncludeSuppliedPlaybooks() {
        ReflectionTestUtils.setField(filterArguments, "suppliedPlaybooks", List.of("VeryLarge", "SecurityHeaders", "Jumbo"));
        List<String> playbooks = filterArguments.getFirstPhasePlaybooksForPath();

        Assertions.assertThat(playbooks).doesNotContain("TopLevelElementsLinter", "HappyPathPlaybook", "RemoveFieldsPlaybook", "Jumbo")
                .containsOnly("CheckSecurityHeadersPlaybook", "VeryLargeStringsInFieldsPlaybook", "VeryLargeUnicodeStringsInFieldsPlaybook", "VeryLargeUnicodeStringsInHeadersPlaybook", "VeryLargeStringsInHeadersPlaybook",
                        "VeryLargeDecimalsInNumericFieldsPlaybook", "VeryLargeIntegersInNumericFieldsPlaybook");
    }

    @Test
    void shouldReturnAllHttpMethodsWhenNotHttpMethodSupplied() {
        List<HttpMethod> httpMethods = filterArguments.getHttpMethods();

        Assertions.assertThat(httpMethods).containsExactlyElementsOf(HttpMethod.restMethods());
    }

    @Test
    void shouldReturnGetAndDeleteWhenNotHttpMethodSupplied() {
        ReflectionTestUtils.setField(filterArguments, "httpMethods", List.of(HttpMethod.GET, HttpMethod.DELETE));
        List<HttpMethod> httpMethods = filterArguments.getHttpMethods();

        Assertions.assertThat(httpMethods).containsOnly(HttpMethod.GET, HttpMethod.DELETE);
    }

    @Test
    void shouldReturnAllRegisteredPlaybooks() {
        Assertions.assertThat(filterArguments.getAllRegisteredPlaybooks()).hasSize(125);
    }

    @Test
    void shouldReturnEmptySkippedPaths() {
        Assertions.assertThat(filterArguments.getSkipPaths()).isEmpty();
    }

    @Test
    void shouldReturnEmptyPaths() {
        Assertions.assertThat(filterArguments.getPaths()).isEmpty();
    }

    @Test
    void shouldSetAllPlaybooksToCustomPlaybook() {
        ReflectionTestUtils.setField(filterArguments, "suppliedPlaybooks", List.of("VeryLarge", "SecurityHeaders", "Jumbo"));
        Assertions.assertThat(filterArguments.getFirstPhasePlaybooksForPath()).contains("VeryLargeUnicodeStringsInFieldsPlaybook");
        FilterArguments.PLAYBOOKS_TO_BE_RUN.clear();
        filterArguments.customFilter("HappyPathPlaybook");
        Assertions.assertThat(filterArguments.getFirstPhasePlaybooksForPath()).doesNotContain("VeryLargeUnicodeStringsInFieldsPlaybook");
    }

    @Test
    void shouldNotGetPlaybooksWhenAllPlaybooksPopulated() {
        HappyPathPlaybook happyPathPlaybook = new HappyPathPlaybook(null);
        FilterArguments.ALL_TEST_CASE_PLAYBOOKS.add(happyPathPlaybook);

        Assertions.assertThat(filterArguments.getAllRegisteredPlaybooks()).containsOnly(happyPathPlaybook);
    }

    @Test
    void shouldNotGetPlaybooksToBeRunWhenPopulated() {
        FilterArguments.PLAYBOOKS_TO_BE_RUN.add("HappyPathPlaybook");
        ReflectionTestUtils.setField(filterArguments, "playbooksToBeRunComputed", true);

        Assertions.assertThat(filterArguments.getFirstPhasePlaybooksForPath()).containsOnly("HappyPathPlaybook");
    }

    @Test
    void shouldRemoveValidateAndSanitizePlaybooks() {
        ReflectionTestUtils.setField(processingArguments, "sanitizationStrategy", ProcessingArguments.SanitizationStrategy.SANITIZE_AND_VALIDATE);
        Assertions.assertThat(filterArguments.removeBasedOnSanitizationStrategy(List.of("AbugidasInStringFieldsSanitizeValidatePlaybook", "AbugidasInStringFieldsValidateSanitizePlaybook")))
                .containsOnly("AbugidasInStringFieldsSanitizeValidatePlaybook");
    }

    @Test
    void shouldRemoveSanitizeAndValidatePlaybooks() {
        ReflectionTestUtils.setField(processingArguments, "sanitizationStrategy", ProcessingArguments.SanitizationStrategy.VALIDATE_AND_SANITIZE);
        Assertions.assertThat(filterArguments.removeBasedOnSanitizationStrategy(List.of("AbugidasInStringFieldsSanitizeValidatePlaybook", "AbugidasInStringFieldsValidateSanitizePlaybook")))
                .containsOnly("AbugidasInStringFieldsValidateSanitizePlaybook");
    }

    @Test
    void shouldRemoveValidateAndTrimPlaybooks() {
        ReflectionTestUtils.setField(processingArguments, "edgeSpacesStrategy", ProcessingArguments.TrimmingStrategy.TRIM_AND_VALIDATE);
        Assertions.assertThat(filterArguments.removeBasedOnTrimStrategy(List.of("LeadingMultiCodePointEmojisInFieldsTrimValidatePlaybook", "LeadingMultiCodePointEmojisInFieldsValidateTrimPlaybook")))
                .containsOnly("LeadingMultiCodePointEmojisInFieldsTrimValidatePlaybook");
    }

    @Test
    void shouldRemoveTrimAndValidatePlaybooks() {
        ReflectionTestUtils.setField(processingArguments, "edgeSpacesStrategy", ProcessingArguments.TrimmingStrategy.VALIDATE_AND_TRIM);
        Assertions.assertThat(filterArguments.removeBasedOnTrimStrategy(List.of("LeadingMultiCodePointEmojisInFieldsTrimValidatePlaybook", "LeadingMultiCodePointEmojisInFieldsValidateTrimPlaybook")))
                .containsOnly("LeadingMultiCodePointEmojisInFieldsValidateTrimPlaybook");
    }

    @Test
    void shouldNotAddSecondPhasePlaybooksInFirstPhase() {
        Assertions.assertThat(filterArguments.getFirstPhasePlaybooksForPath()).doesNotContain("CheckDeletedResourcesNotAvailablePlaybook");
    }

    @Test
    void shouldNotAddFirstPhasePlaybooksInSecondPhase() {
        Assertions.assertThat(filterArguments.getSecondPhasePlaybooks()).hasOnlyElementsOfType(DeletedResourcesNotAvailablePlaybook.class);
    }

    @Test
    void shouldReturnEmptySecondPhaseWhenSpecialPlaybook() {
        Assertions.assertThat(filterArguments.getSecondPhasePlaybooks()).hasOnlyElementsOfType(DeletedResourcesNotAvailablePlaybook.class);
        filterArguments.customFilter("RandomPlaybook");
        Assertions.assertThat(filterArguments.getSecondPhasePlaybooks()).isEmpty();
    }

    @Test
    void shouldOnlyReturnSpecialPlaybook() {
        filterArguments.customFilter("HappyPathPlaybook");
        Assertions.assertThat(filterArguments.getFirstPhasePlaybooksForPath()).containsOnly("HappyPathPlaybook");
    }

    @Test
    void shouldHave4FieldTypes() {
        Assertions.assertThat(FilterArguments.FieldType.values()).hasSize(4);
    }

    @Test
    void shouldHave16FieldFormats() {
        Assertions.assertThat(FilterArguments.FormatType.values()).hasSize(16);
    }

    @Test
    void shouldReturnSkippedFields() {
        ReflectionTestUtils.setField(filterArguments, "skipFields", List.of("field1", "field2"));

        List<String> skipFields = filterArguments.getSkipFields();
        Assertions.assertThat(skipFields).containsOnly("field1", "field2");
    }

    @Test
    void shouldReturnSkippedFieldsForAllPlaybooks() {
        ReflectionTestUtils.setField(filterArguments, "skipFields", List.of("field1", "!field2"));

        List<String> skipFields = filterArguments.getSkipFields();
        List<String> skipFieldsForAllPlaybooks = filterArguments.getSkipFieldsToBeSkippedForAllPlaybooks();
        Assertions.assertThat(skipFields).containsOnly("field1", "!field2");
        Assertions.assertThat(skipFieldsForAllPlaybooks).containsOnly("field2");
    }

    @Test
    void shouldReturnEmptySkipFields() {
        Assertions.assertThat(filterArguments.getSkipFields()).isEmpty();
    }

    @Test
    void shouldReturnEmptySkipHeaders() {
        Assertions.assertThat(filterArguments.getSkipHeaders()).isEmpty();
    }

    @Test
    void shouldNotFilter() {
        Paths paths = new Paths();
        paths.addPathItem("/path1", new PathItem());
        paths.addPathItem("/path2", new PathItem());
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(openAPI.getPaths()).thenReturn(paths);

        List<String> filteredPaths = filterArguments.getPathsToRun(openAPI);
        Assertions.assertThat(filteredPaths).containsOnly("/path1", "/path2");
    }

    @Test
    void shouldCachePathsToRun() {
        Paths paths = new Paths();
        paths.addPathItem("/path1", new PathItem());
        paths.addPathItem("/path11", new PathItem());
        paths.addPathItem("/path2", new PathItem());
        ReflectionTestUtils.setField(filterArguments, "paths", List.of("/path1"));

        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(openAPI.getPaths()).thenReturn(paths);

        List<String> filteredPaths = filterArguments.getPathsToRun(openAPI);
        Assertions.assertThat(filteredPaths).containsOnly("/path1");
        ReflectionTestUtils.setField(filterArguments, "paths", List.of("/path1", "/path11", "/path2"));

        List<String> secondTimeFilteredPaths = filterArguments.getPathsToRun(openAPI);
        Assertions.assertThat(secondTimeFilteredPaths).containsOnly("/path1");

        FilterArguments.PATHS_TO_INCLUDE.clear();
        List<String> thirdTimeFilteredPaths = filterArguments.getPathsToRun(openAPI);
        Assertions.assertThat(thirdTimeFilteredPaths).containsOnly("/path1", "/path11", "/path2");
    }

    @Test
    void shouldOnlyIncludeSuppliedPaths() {
        Paths paths = new Paths();
        paths.addPathItem("/path1", new PathItem());
        paths.addPathItem("/path11", new PathItem());
        paths.addPathItem("/path2", new PathItem());
        ReflectionTestUtils.setField(filterArguments, "paths", List.of("/path1"));

        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(openAPI.getPaths()).thenReturn(paths);

        List<String> filteredPaths = filterArguments.getPathsToRun(openAPI);
        Assertions.assertThat(filteredPaths).containsOnly("/path1");
    }

    @Test
    void shouldExcludeSkipPaths() {
        Paths paths = new Paths();
        paths.addPathItem("/path1", new PathItem());
        paths.addPathItem("/path2", new PathItem());
        ReflectionTestUtils.setField(filterArguments, "skipPaths", List.of("/path1"));

        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(openAPI.getPaths()).thenReturn(paths);

        List<String> filteredPaths = filterArguments.getPathsToRun(openAPI);
        Assertions.assertThat(filteredPaths).containsOnly("/path2");
    }

    @ParameterizedTest
    @CsvSource({"/path1*,/path11", "/path2*,/path2", "*path3,/another-path3", "*another*,/another-path3"})
    void shouldIncludeWildcard(String wildcardPattern, String result) {
        Paths paths = new Paths();
        paths.addPathItem("/path11", new PathItem());
        paths.addPathItem("/path2", new PathItem());
        paths.addPathItem("/another-path3", new PathItem());
        ReflectionTestUtils.setField(filterArguments, "paths", List.of(wildcardPattern));

        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Mockito.when(openAPI.getPaths()).thenReturn(paths);

        List<String> filteredPaths = filterArguments.getPathsToRun(openAPI);
        Assertions.assertThat(filteredPaths).containsOnly(result);
    }

    @Test
    void shouldHaveDefaultHttpMethods() {
        Assertions.assertThat(filterArguments.isHttpMethodSupplied(HttpMethod.POST)).isTrue();
        Assertions.assertThat(filterArguments.isHttpMethodSupplied(HttpMethod.BIND)).isFalse();
    }

    @Test
    void shouldReturnPlaybooksAsClasses() {
        Assertions.assertThat(filterArguments.getFirstPhasePlaybooksAsPlaybooks()).hasSize(123);
    }

    @Test
    void shouldNotReturnAllPlaybooks() {
        Set<HttpMethod> httpMethods = Set.of(HttpMethod.GET);
        List<TestCasePlaybook> filteredTestCasePlaybooks = filterArguments.filterOutPlaybooksNotMatchingHttpMethodsAndPath(httpMethods, "");
        int allPlaybooksSize = filterArguments.getFirstPhasePlaybooksAsPlaybooks().size();
        int filteredSize = filteredTestCasePlaybooks.size();
        Assertions.assertThat(filteredSize).isNotEqualTo(allPlaybooksSize);
    }

    @Test
    void shouldReturnAllPlaybooks() {
        Set<HttpMethod> httpMethods = Set.of(HttpMethod.GET, HttpMethod.POST);
        List<TestCasePlaybook> filteredTestCasePlaybooks = filterArguments.filterOutPlaybooksNotMatchingHttpMethodsAndPath(httpMethods, "");
        int allPlaybooksSize = filterArguments.getFirstPhasePlaybooksAsPlaybooks().size();
        Assertions.assertThat(filteredTestCasePlaybooks).hasSize(allPlaybooksSize);
    }

    @Test
    void shouldSkipHttpMethods() {
        List<HttpMethod> skipped = List.of(HttpMethod.DELETE);
        filterArguments.setSkippedHttpMethods(skipped);

        Assertions.assertThat(filterArguments.getHttpMethods()).doesNotContain(HttpMethod.DELETE);
    }

    @Test
    void shouldCountTotalPlaybooks() {
        Assertions.assertThat(filterArguments.getTotalPlaybooks()).isEqualTo(116);
    }
}