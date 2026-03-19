package dev.dochia.cli.core.report;

import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.TestCase;
import dev.dochia.cli.core.model.TestCaseSummary;
import dev.dochia.cli.core.util.DochiaRandom;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class BucketsCalculatorTest {

    @BeforeEach
    void setUp() {
        DochiaRandom.initRandom(0);
    }

    @Test
    void testCreateBucketsWithDifferentResultReasons() {
        List<TestCaseSummary> testCases = List.of(
                createTestCaseSummary(404, "Not Found", "error", "Error: File not found at /path/to/file"),
                createTestCaseSummary(404, "Resource Missing", "error", "Error: Resource not available")
        );

        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);

        assertThat(buckets).hasSize(2);
        assertThat(buckets.get(0).get("resultReason")).isIn("Not Found", "Resource Missing");
        assertThat(buckets.get(1).get("resultReason")).isIn("Not Found", "Resource Missing");
    }

    @Test
    void testCreateBucketsWithWarningsCases() {
        List<TestCaseSummary> testCases = List.of(
                createTestCaseSummary(400, "Bad Request", "warning", "Warning: Potential data inconsistency"),
                createTestCaseSummary(400, "Bad Request", "warning", "Warning: Potential data inconsistency")
        );

        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);

        assertThat(buckets).hasSize(1);
        assertThat(buckets.getFirst()).containsEntry("resultReason", "Bad Request");
    }

    @Test
    void testCreateBucketsBucketProperties() {
        List<TestCaseSummary> testCases = List.of(
                createTestCaseSummary(404, "Not Found", "error", "Error: File not found at /path/to/file"),
                createTestCaseSummary(404, "Not Found", "error", "Error: File not found at /another/path")
        );

        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);

        assertThat(buckets).hasSize(1);
        Map<String, Object> bucket = ((List<Map<String, Object>>) buckets.getFirst().get("buckets")).getFirst();

        assertThat(bucket).containsKeys("bucketId", "errorMessage", "borderColor", "paths");
        assertThat(bucket.get("paths")).isInstanceOf(List.class);
    }

    @Test
    void testCreateBucketsWithMixedSuccessAndErrorCases() {
        List<TestCaseSummary> testCases = List.of(
                createTestCaseSummary(200, "OK", "success", "Success"),
                createTestCaseSummary(404, "Not Found", "error", "Error: Resource not found"),
                createTestCaseSummary(500, "Server Error", "error", "Error: Internal server error")
        );

        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);

        assertThat(buckets).hasSize(2);  // One for "Not Found", one for "Server Error"
    }

    @Test
    void testCreateBucketsWithComplexErrorScenarios() {
        List<TestCaseSummary> testCases = List.of(
                createTestCaseSummary(400, "Bad Request", "error", "Error: Invalid input"),
                createTestCaseSummary(400, "Bad Request", "error", "Error: Invalid input format"),
                createTestCaseSummary(404, "Not Found", "error", "Error: Resource missing"),
                createTestCaseSummary(500, "Server Error", "error", "Error: Database connection failed")
        );

        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);

        assertThat(buckets).hasSize(3);
        assertThat(buckets.stream().map(c -> c.get("resultReason")))
                .containsExactlyInAnyOrder("Bad Request", "Not Found", "Server Error");
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesAndExpectedBucketSize")
    void testCreateBuckets(List<TestCaseSummary> testCases, int expectedBucketSize) {
        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);
        assertThat(buckets).hasSize(expectedBucketSize);
    }

    @ParameterizedTest
    @MethodSource("provideSimilarErrorTestCases")
    void testCreateBucketsWithSimilarErrors(List<TestCaseSummary> testCases, int expectedBucketCount) {
        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);
        assertThat(buckets).hasSize(expectedBucketCount);
    }

    private static Stream<Arguments> provideTestCasesAndExpectedBucketSize() {
        return Stream.of(
                Arguments.of(new ArrayList<TestCaseSummary>(), 0),

                Arguments.of(
                        List.of(
                                createTestCaseSummary(404, "Not Found", "error", "Error: File not found at /path/to/file"),
                                createTestCaseSummary(500, "Internal Server Error", "error", "Error: Unable to connect to database")
                        ),
                        2
                ),

                Arguments.of(
                        List.of(
                                createTestCaseSummary(200, "OK", "success", "Success"),
                                createTestCaseSummary(201, "Created", "success", "Resource created")
                        ),
                        0
                ),

                Arguments.of(
                        List.of(
                                createTestCaseSummary(404, "Not Found", "error", "Error: File not found at /path/to/file"),
                                createTestCaseSummary(200, "OK", "success", "Success"),
                                createTestCaseSummary(500, "Internal Server Error", "error", "Error: Unable to connect to database")
                        ),
                        2
                ),

                Arguments.of(
                        List.of(
                                createTestCaseSummary(404, "Not Found", "error", "Error: File not found at /path/to/file"),
                                createTestCaseSummary(500, "Internal Server Error", "error", "Error: Unable to connect to database")
                        ),
                        2
                ),

                Arguments.of(
                        List.of(
                                createTestCaseSummary(404, "Not Found", "error", ""),
                                createTestCaseSummary(500, "Internal Server Error", "error", "")
                        ),
                        2
                ),

                Arguments.of(
                        List.of(
                                createTestCaseSummary(404, "Not Found", "error", null),
                                createTestCaseSummary(500, "Internal Server Error", "error", null)
                        ),
                        2
                )
        );
    }

    private static Stream<Arguments> provideSimilarErrorTestCases() {
        return Stream.of(
                Arguments.of(
                        List.of(
                                createTestCaseSummary(404, "Not Found", "error", "Error: File not found at /path/to/file"),
                                createTestCaseSummary(404, "Not Found", "error", "Error: File not found at /another/path/to/file")
                        ),
                        1
                ),

                Arguments.of(
                        List.of(
                                createTestCaseSummary(404, "Not Found", "error", "Error: File not found at /path/to/file"),
                                createTestCaseSummary(404, "Not Found", "error", "Error: File not found at /path/to/file"),
                                createTestCaseSummary(500, "Internal Server Error", "error", "Error: Unable to connect to database")
                        ),
                        2
                )
        );
    }

    @Test
    void testCreateBucketsWithLargeNumberOfSimilarErrors() {
        List<TestCaseSummary> testCases = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            testCases.add(createTestCaseSummary(404, "Not Found", "error", "Error: File not found at /path/" + i));
        }

        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);

        assertThat(buckets).hasSize(1);
        List<Map<String, Object>> bucketList = (List<Map<String, Object>>) buckets.getFirst().get("buckets");
        assertThat(bucketList).hasSize(1);
        assertThat(bucketList.getFirst().get("paths")).isInstanceOf(List.class);
    }

    @Test
    void testCreateBucketsWithMultiplePatternGroups() {
        List<TestCaseSummary> testCases = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            testCases.add(createTestCaseSummary(404, "Not Found", "error", "Error: File not found at /path/" + i));
        }
        for (int i = 0; i < 10; i++) {
            testCases.add(createTestCaseSummary(404, "Not Found", "error", "Warning: Resource unavailable for id " + i));
        }
        for (int i = 0; i < 10; i++) {
            testCases.add(createTestCaseSummary(404, "Not Found", "error", "Exception: Database connection timeout after " + i + " seconds"));
        }

        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);

        assertThat(buckets).hasSize(1);
        List<Map<String, Object>> bucketList = (List<Map<String, Object>>) buckets.getFirst().get("buckets");
        assertThat(bucketList).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void testCreateBucketsWithVeryLongErrorMessages() {
        String longError1 = "Error: " + "A".repeat(300) + " occurred at line 123";
        String longError2 = "Error: " + "A".repeat(300) + " occurred at line 456";
        String longError3 = "Error: " + "B".repeat(300) + " occurred at line 789";

        List<TestCaseSummary> testCases = List.of(
                createTestCaseSummary(500, "Server Error", "error", longError1),
                createTestCaseSummary(500, "Server Error", "error", longError2),
                createTestCaseSummary(500, "Server Error", "error", longError3)
        );

        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);

        assertThat(buckets).hasSize(1);
        List<Map<String, Object>> bucketList = (List<Map<String, Object>>) buckets.getFirst().get("buckets");
        assertThat(bucketList).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testCreateBucketsWithSingleTestCaseInGroup() {
        List<TestCaseSummary> testCases = List.of(
                createTestCaseSummary(404, "Not Found", "error", "Error: Unique error message")
        );

        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);

        assertThat(buckets).hasSize(1);
        List<Map<String, Object>> bucketList = (List<Map<String, Object>>) buckets.getFirst().get("buckets");
        assertThat(bucketList).hasSize(1);
    }

    @Test
    void testCreateBucketsWithEmptyResponseBodies() {
        List<TestCaseSummary> testCases = List.of(
                createTestCaseSummary(404, "Not Found", "error", ""),
                createTestCaseSummary(404, "Not Found", "error", ""),
                createTestCaseSummary(404, "Not Found", "error", "   ")
        );

        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);

        assertThat(buckets).hasSize(1);
        List<Map<String, Object>> bucketList = (List<Map<String, Object>>) buckets.getFirst().get("buckets");
        assertThat(bucketList).hasSize(1);
    }

    @Test
    void testCreateBucketsWithMixedEmptyAndNonEmptyResponses() {
        List<TestCaseSummary> testCases = List.of(
                createTestCaseSummary(404, "Not Found", "error", "Error: File not found"),
                createTestCaseSummary(404, "Not Found", "error", ""),
                createTestCaseSummary(404, "Not Found", "error", null),
                createTestCaseSummary(404, "Not Found", "error", "Error: File not found at different location")
        );

        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);

        assertThat(buckets).hasSize(1);
        List<Map<String, Object>> bucketList = (List<Map<String, Object>>) buckets.getFirst().get("buckets");
        assertThat(bucketList).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testCreateBucketsWithDifferentPathsSameError() {
        List<TestCaseSummary> testCases = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TestCaseSummary tc = createTestCaseSummary(404, "Not Found", "error", "Error: Resource not found");
            testCases.add(tc);
        }

        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);

        assertThat(buckets).hasSize(1);
        List<Map<String, Object>> bucketList = (List<Map<String, Object>>) buckets.getFirst().get("buckets");
        assertThat(bucketList).hasSize(1);
        Map<String, Object> bucket = bucketList.getFirst();
        List<Map<String, Object>> paths = (List<Map<String, Object>>) bucket.get("paths");
        assertThat(paths).isNotEmpty();
    }

    @Test
    void testCreateBucketsWithComparisonTrackerGrowth() {
        List<TestCaseSummary> testCases = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            testCases.add(createTestCaseSummary(404, "Not Found", "error", "Error type " + (i % 5) + ": details " + i));
        }

        List<Map<String, Object>> buckets = BucketsCalculator.createBuckets(testCases);

        assertThat(buckets).hasSize(1);
        List<Map<String, Object>> bucketList = (List<Map<String, Object>>) buckets.getFirst().get("buckets");
        assertThat(bucketList).isNotEmpty();
    }

    private static TestCaseSummary createTestCaseSummary(int httpResponseCode, String resultReason, String result, String responseBody) {
        TestCase testCase = new TestCase();
        testCase.setResponse(HttpResponse.builder().responseCode(httpResponseCode).body(responseBody).build());
        testCase.setResult(result);
        testCase.setPath("/path/" + DochiaRandom.instance().nextInt(1000));
        testCase.setContractPath(testCase.getPath());
        testCase.setResultReason(resultReason);
        testCase.setTestId(DochiaRandom.instance().nextInt(2000) + "");
        return TestCaseSummary.fromTestCase(testCase);
    }
}
