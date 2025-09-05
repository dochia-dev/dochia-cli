package dev.dochia.cli.core.report;


import dev.dochia.cli.core.model.TestCaseSummary;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.WordUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Utility class for grouping test cases based on error similarity.
 */
public class BucketsCalculator {

    private static final int MIN_HTTP_SUCCESS_CODE = 200;
    private static final int MAX_HTTP_SUCCESS_CODE = 300;

    private BucketsCalculator() {
        // Prevent instantiation of utility class
    }

    /**
     * Creates buckets of test cases with similar errors.
     *
     * @param testCases List of test case summaries to bucket
     * @return List of bucketed test case groups
     */
    public static List<Map<String, Object>> createBuckets(List<TestCaseSummary> testCases) {
        // Filter test cases with non-2xx responses and errors
        List<TestCaseSummary> non2xxCases = filterNon2xxTestCases(testCases);

        // Group test cases by their result reason
        Map<String, List<TestCaseSummary>> groupedByReason = groupTestCasesByReason(non2xxCases);

        return createClusterResultList(groupedByReason);
    }

    /**
     * Filters test cases with non-2xx HTTP responses and containing errors.
     *
     * @param testCases List of test cases to filter
     * @return Filtered list of test cases
     */
    private static List<TestCaseSummary> filterNon2xxTestCases(List<TestCaseSummary> testCases) {
        return testCases.stream()
                .filter(tc ->
                        (tc.getHttpResponseCode() < MIN_HTTP_SUCCESS_CODE || tc.getHttpResponseCode() >= MAX_HTTP_SUCCESS_CODE) &&
                                StringUtils.isNotBlank(tc.getResultReason()) &&
                                (tc.getError() || tc.getWarning())
                )
                .toList();
    }

    /**
     * Groups test cases by their result reason.
     *
     * @param testCases List of test cases to group
     * @return Map of test cases grouped by result reason
     */
    private static Map<String, List<TestCaseSummary>> groupTestCasesByReason(List<TestCaseSummary> testCases) {
        return testCases.stream()
                .collect(Collectors.groupingBy(TestCaseSummary::getResultReason));
    }

    /**
     * Creates a list of bucketed test case results.
     *
     * @param groupedByReason Map of test cases grouped by result reason
     * @return List of bucketed test case groups
     */
    private static List<Map<String, Object>> createClusterResultList(
            Map<String, List<TestCaseSummary>> groupedByReason) {

        return groupedByReason.entrySet().stream()
                .map(entry -> createResultMapForReason(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * Creates a result map for a specific reason and its test cases.
     *
     * @param resultReason       Reason for the test cases
     * @param testCasesForReason List of test cases for this reason
     * @return Map representing the result
     */
    private static Map<String, Object> createResultMapForReason(
            String resultReason, List<TestCaseSummary> testCasesForReason) {

        List<Map<String, Object>> bucketList = createClustersForTestCases(testCasesForReason);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultReason", resultReason);
        resultMap.put("buckets", bucketList);
        return resultMap;
    }

    /**
     * Creates buckets of test cases based on error similarity.
     *
     * @param testCasesForReason List of test cases to bucket
     * @return List of bucket maps
     */
    private static List<Map<String, Object>> createClustersForTestCases(
            List<TestCaseSummary> testCasesForReason) {

        List<TestCaseSummary> emptyResponseCases = new ArrayList<>();
        List<TestCaseSummary> nonEmptyResponseCases = new ArrayList<>();

        // Separate empty and non-empty response cases
        separateResponseCases(testCasesForReason, emptyResponseCases, nonEmptyResponseCases);

        // Cluster similar error cases
        List<List<TestCaseSummary>> similarityClusters =
                bucketBySimilarity(nonEmptyResponseCases, WordUtils::areErrorsSimilar);

        // Add empty response cases to buckets if present
        if (!emptyResponseCases.isEmpty()) {
            similarityClusters.add(emptyResponseCases);
        }

        return createClusterMaps(similarityClusters);
    }

    /**
     * Separates test cases into empty and non-empty response cases.
     *
     * @param testCases             Source list of test cases
     * @param emptyResponseCases    Destination list for empty response cases
     * @param nonEmptyResponseCases Destination list for non-empty response cases
     */
    private static void separateResponseCases(
            List<TestCaseSummary> testCases,
            List<TestCaseSummary> emptyResponseCases,
            List<TestCaseSummary> nonEmptyResponseCases) {

        for (TestCaseSummary tc : testCases) {
            if (tc.getResponseBody() == null || tc.getResponseBody().trim().isEmpty()) {
                emptyResponseCases.add(tc);
            } else {
                nonEmptyResponseCases.add(tc);
            }
        }
    }

    /**
     * Creates bucket maps for given buckets of test cases.
     *
     * @param similarityClusters List of test case buckets
     * @return List of bucket maps
     */
    private static List<Map<String, Object>> createClusterMaps(
            List<List<TestCaseSummary>> similarityClusters) {

        List<Map<String, Object>> bucketList = new ArrayList<>();
        int bucketCounter = 1;

        for (List<TestCaseSummary> bucket : similarityClusters) {
            Map<String, Object> bucketMap = new HashMap<>();
            bucketMap.put("bucketId", bucketCounter++);

            String representativeError = bucket.getFirst().getResponseBody();
            bucketMap.put("errorMessage", representativeError);
            bucketMap.put("borderColor", generateRandomHexColor());

            bucketMap.put("paths", createPathList(bucket));
            bucketList.add(bucketMap);
        }

        return bucketList;
    }

    /**
     * Creates a list of path information for a bucket of test cases.
     *
     * @param bucket List of test cases in a bucket
     * @return List of path maps
     */
    private static List<Map<String, Object>> createPathList(List<TestCaseSummary> bucket) {
        Map<String, StringBuilder> pathGroups = new HashMap<>();

        for (TestCaseSummary tc : bucket) {
            String path = tc.getPath();
            pathGroups.computeIfAbsent(path, k -> new StringBuilder())
                    .append(!pathGroups.get(path).isEmpty() ? ", " : "")
                    .append(String.format("<a href=\"%s.html\" target=\"_blank\">%s</a>",
                            tc.getKey(), tc.getId()));
        }

        return pathGroups.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> pathMap = new HashMap<>();
                    pathMap.put("path", entry.getKey());
                    pathMap.put("testCases", entry.getValue().toString());
                    return pathMap;
                })
                .toList();
    }

    /**
     * Clusters test cases by similarity using the provided similarity checker.
     *
     * @param testCases         List of test cases to bucket
     * @param similarityChecker Predicate to determine error similarity
     * @return List of test case buckets
     */
    private static List<List<TestCaseSummary>> bucketBySimilarity(
            List<TestCaseSummary> testCases,
            BiPredicate<String, String> similarityChecker) {

        if (testCases.isEmpty()) {
            return new ArrayList<>();
        }

        if (testCases.size() == 1) {
            return new ArrayList<>(List.of(testCases));
        }

        List<List<TestCaseSummary>> buckets = new ArrayList<>();
        Map<TestCaseSummary, Boolean[]> comparisonTracker = new HashMap<>();

        for (TestCaseSummary current : testCases) {
            if (current.getResponseBody() == null || current.getResponseBody().trim().isEmpty()) {
                continue;
            }

            boolean addedToCluster = tryAddToExistingCluster(current, buckets, comparisonTracker, similarityChecker);

            if (!addedToCluster) {
                List<TestCaseSummary> newCluster = new ArrayList<>();
                newCluster.add(current);
                buckets.add(newCluster);
            }
        }

        return buckets;
    }

    /**
     * Attempts to add a test case to an existing bucket based on similarity.
     *
     * @param current           Current test case to add
     * @param buckets          Existing buckets
     * @param comparisonTracker Tracker for previous comparisons
     * @param similarityChecker Predicate to determine error similarity
     * @return True if added to a bucket, false otherwise
     */
    private static boolean tryAddToExistingCluster(
            TestCaseSummary current,
            List<List<TestCaseSummary>> buckets,
            Map<TestCaseSummary, Boolean[]> comparisonTracker,
            BiPredicate<String, String> similarityChecker) {

        for (int bucketIndex = 0; bucketIndex < buckets.size(); bucketIndex++) {
            List<TestCaseSummary> bucket = buckets.get(bucketIndex);
            TestCaseSummary representative = bucket.getFirst();

            Boolean[] comparisons = getOrCreateComparisons(current, comparisonTracker, buckets.size());
            Boolean previousComparison = comparisons[bucketIndex];

            if (previousComparison != null) {
                if (previousComparison) {
                    bucket.add(current);
                    return true;
                }
                continue;
            }

            boolean similar = similarityChecker.test(representative.getResponseBody(), current.getResponseBody());
            comparisons[bucketIndex] = similar;

            if (similar) {
                bucket.add(current);
                return true;
            }
        }

        return false;
    }

    /**
     * Gets or creates comparison tracking array for a test case.
     *
     * @param current           Current test case
     * @param comparisonTracker Existing comparison tracker
     * @param bucketSize       Number of buckets
     * @return Comparison tracking array
     */
    private static Boolean[] getOrCreateComparisons(
            TestCaseSummary current,
            Map<TestCaseSummary, Boolean[]> comparisonTracker,
            int bucketSize) {

        Boolean[] comparisons = comparisonTracker.get(current);
        if (comparisons == null) {
            comparisons = new Boolean[bucketSize];
            comparisonTracker.put(current, comparisons);
        } else if (comparisons.length < bucketSize) {
            Boolean[] newComparisons = new Boolean[bucketSize];
            System.arraycopy(comparisons, 0, newComparisons, 0, comparisons.length);
            comparisons = newComparisons;
            comparisonTracker.put(current, comparisons);
        }
        return comparisons;
    }

    /**
     * Generates a random hex color.
     *
     * @return Random hex color string
     */
    private static String generateRandomHexColor() {
        int r = CommonUtils.random().nextInt(256);
        int g = CommonUtils.random().nextInt(256);
        int b = CommonUtils.random().nextInt(256);
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
