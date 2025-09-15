package dev.dochia.cli.core.report;

import dev.dochia.cli.core.model.TestCaseSummary;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class BucketsCalculatorTest {
    // Helper to create a TestCaseSummary with minimal fields
    private static TestCaseSummary summary(String id, int code, String reason, String body, boolean error, boolean warning, String path) {
        TestCaseSummary t = new TestCaseSummary();
        setField(t, "id", id);
        setField(t, "httpResponseCode", code);
        setField(t, "resultReason", reason);
        setField(t, "responseBody", body);
        setField(t, "path", path);
        setField(t, "result", error ? "FAILED" : "PASSED");
        setField(t, "switchedResult", false);
        setField(t, "playbook", "pb");
        setField(t, "scenario", "sc");
        setField(t, "resultDetails", "details");
        setField(t, "httpMethod", "get");
        setField(t, "timeToExecuteInSec", 1.0);
        setField(t, "timeToExecuteInMs", 1000L);
        // error/warning are not fields, but logic uses getError()/getWarning() - simulate via result string
        return new TestCaseSummary() {
            @Override
            public boolean getError() {
                return error;
            }

            @Override
            public boolean getWarning() {
                return warning;
            }

            @Override
            public String getId() {
                return id;
            }

            @Override
            public int getHttpResponseCode() {
                return code;
            }

            @Override
            public String getResultReason() {
                return reason;
            }

            @Override
            public String getResponseBody() {
                return body;
            }

            @Override
            public String getPath() {
                return path;
            }

            @Override
            public String getResult() {
                return error ? "FAILED" : "PASSED";
            }

            @Override
            public String getKey() {
                return id == null ? "" : id.replace("/", "-");
            }
        };
    }

    private static void setField(Object obj, String field, Object value) {
        try {
            var f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception ignored) {
            //ignore as it doesn't matter for the test
        }
    }

    @Test
    void testEmptyInput() {
        List<Map<String, Object>> result = BucketsCalculator.createBuckets(List.of());
        assertThat(result).isEmpty();
    }

    @Test
    void testAll2xxFiltered() {
        var t1 = summary("1", 200, "reason", "body", true, false, "/a");
        var t2 = summary("2", 201, "reason", "body", true, false, "/b");
        var t3 = summary("3", 299, "reason", "body", true, false, "/c");
        List<Map<String, Object>> result = BucketsCalculator.createBuckets(List.of(t1, t2, t3));
        assertThat(result).isEmpty();
    }

    @Test
    void testNon2xxWithoutErrorOrWarningFiltered() {
        var t1 = summary("1", 404, "reason", "body", false, false, "/a");
        var t2 = summary("2", 500, "reason", "body", false, false, "/b");
        List<Map<String, Object>> result = BucketsCalculator.createBuckets(List.of(t1, t2));
        assertThat(result).isEmpty();
    }

    @Test
    void testNon2xxWithoutReasonFiltered() {
        var t1 = summary("1", 404, null, "body", true, false, "/a");
        var t2 = summary("2", 500, "", "body", true, false, "/b");
        List<Map<String, Object>> result = BucketsCalculator.createBuckets(List.of(t1, t2));
        assertThat(result).isEmpty();
    }

    @Test
    void testBucketsByReason() {
        var t1 = summary("1", 404, "reason1", "body1", true, false, "/a");
        var t2 = summary("2", 500, "reason2", "body2", true, false, "/b");
        var t3 = summary("3", 400, "reason1", "body3", true, false, "/c");
        List<Map<String, Object>> result = BucketsCalculator.createBuckets(List.of(t1, t2, t3));
        assertThat(result).hasSize(2)
                .anySatisfy(map -> assertThat(map).containsEntry("resultReason", "reason1"))
                .anySatisfy(map -> assertThat(map).containsEntry("resultReason", "reason2"));
    }

    @Test
    void testBucketsByEmptyResponseBody() {
        var t1 = summary("1", 404, "reason", "", true, false, "/a");
        var t2 = summary("2", 404, "reason", null, true, false, "/b");
        var t3 = summary("3", 404, "reason", " ", true, false, "/c");
        List<Map<String, Object>> result = BucketsCalculator.createBuckets(List.of(t1, t2, t3));
        assertThat(result).hasSize(1);
        var buckets = (List<?>) result.getFirst().get("buckets");
        assertThat(buckets).hasSize(1);
        Map<String, Object> bucket = (Map<String, Object>) buckets.getFirst();
        assertThat(bucket).containsEntry("errorMessage", "<empty response body>");
    }

    @Test
    void testBucketsByExactResponseBody() {
        var t1 = summary("1", 404, "reason", "body", true, false, "/a");
        var t2 = summary("2", 404, "reason", "body", true, false, "/b");
        List<Map<String, Object>> result = BucketsCalculator.createBuckets(List.of(t1, t2));
        var buckets = (List<?>) result.getFirst().get("buckets");
        assertThat(buckets).hasSize(1);
        Map<String, Object> bucket = (Map<String, Object>) buckets.getFirst();
        assertThat(bucket).containsEntry("errorMessage", "body");
        List<String> paths = (List<String>) bucket.get("paths");
        assertThat(paths).hasSize(2);
    }

    @ParameterizedTest
    @MethodSource("bucketsSimilarityProvider")
    void testBucketsSimilarity(List<TestCaseSummary> summaries, int expectedBucketCount) {
        List<Map<String, Object>> result = BucketsCalculator.createBuckets(summaries);
        var buckets = (List<?>) result.getFirst().get("buckets");
        assertThat(buckets).hasSize(expectedBucketCount);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> bucketsSimilarityProvider() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(
                        List.of(
                                summary("1", 404, "reason", "Error: ID=123", true, false, "/a"),
                                summary("2", 404, "reason", "Error: ID=456", true, false, "/b")
                        ),
                        1
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        List.of(
                                summary("1", 404, "reason", "foo bar baz", true, false, "/a"),
                                summary("2", 404, "reason", "foo bar qux", true, false, "/b")
                        ),
                        2
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        List.of(
                                summary("1", 404, "reason", "Error: code 123", true, false, "/a"),
                                summary("2", 404, "reason", "Error: code 124", true, false, "/b")
                        ),
                        1
                ),
                org.junit.jupiter.params.provider.Arguments.of(
                        List.of(
                                summary("1", 404, "reason", "foo", true, false, "/a"),
                                summary("2", 404, "reason", "bar", true, false, "/b")
                        ),
                        2
                )
        );
    }

    @Test
    void testPathGroupingAndHtmlLinks() {
        var t1 = summary("1", 404, "reason", "body", true, false, "/api/foo");
        var t2 = summary("2", 404, "reason", "body", true, false, "/api/foo");
        var t3 = summary("3", 404, "reason", "body", true, false, "/api/bar");
        List<Map<String, Object>> result = BucketsCalculator.createBuckets(List.of(t1, t2, t3));
        var buckets = (List<?>) result.get(0).get("buckets");
        var bucket = (Map<?, ?>) buckets.get(0);
        var paths = (List<?>) bucket.get("paths");
        assertThat(paths).hasSize(2);
        var foo = (Map<?, ?>) paths.stream().filter(p -> ((Map<?, ?>) p).get("path").equals("/api/foo")).findFirst().orElseThrow();
        assertThat((String) foo.get("testCases")).contains("<a href=\"1.html").contains("<a href=\"2.html");
        var bar = (Map<?, ?>) paths.stream().filter(p -> ((Map<?, ?>) p).get("path").equals("/api/bar")).findFirst().orElseThrow();
        assertThat((String) bar.get("testCases")).contains("<a href=\"3.html");
    }

    @Test
    void testColorFormat() {
        var t1 = summary("1", 404, "reason", "body", true, false, "/a");
        List<Map<String, Object>> result = BucketsCalculator.createBuckets(List.of(t1));
        var buckets = (List<?>) result.get(0).get("buckets");
        var bucket = (Map<?, ?>) buckets.get(0);
        String color = (String) bucket.get("borderColor");
        assertThat(color).matches(Pattern.compile("#([0-9a-fA-F]{6})"));
    }
}
