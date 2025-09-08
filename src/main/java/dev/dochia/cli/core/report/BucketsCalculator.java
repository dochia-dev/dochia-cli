package dev.dochia.cli.core.report;

import dev.dochia.cli.core.model.TestCaseSummary;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.JaccardSimilarity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Groups test cases by resultReason, then by similar response bodies.
 */
public final class BucketsCalculator {

    private BucketsCalculator() {
    }

    private static final int MIN_HTTP_SUCCESS_CODE = 200;
    private static final int MAX_HTTP_SUCCESS_CODE = 300;

    private static final double JACCARD_GATE = WordUtils.JACCARD_THRESHOLD; // align with WordUtils
    private static final JaccardSimilarity JS = new JaccardSimilarity();

    private static final String EMPTY_BODY_LABEL = "<empty response body>";

    /**
     * Groups test cases by resultReason, then by similar response bodies.
     *
     * @param testCases the list of test case summaries
     * @return the list of bucketed test case groups
     */
    public static List<Map<String, Object>> createBuckets(List<TestCaseSummary> testCases) {
        List<TestCaseSummary> non2xxCases = testCases.stream()
                .filter(tc ->
                        (tc.getHttpResponseCode() < MIN_HTTP_SUCCESS_CODE
                                || tc.getHttpResponseCode() >= MAX_HTTP_SUCCESS_CODE)
                                && (tc.getError() || tc.getWarning())
                                && StringUtils.isNotBlank(tc.getResultReason()))
                .toList();

        Map<String, List<TestCaseSummary>> byReason =
                non2xxCases.stream().collect(Collectors.groupingBy(TestCaseSummary::getResultReason));

        return byReason.entrySet().parallelStream()
                .map(e -> createResultMapForReason(e.getKey(), e.getValue()))
                .toList();
    }

    private static Map<String, Object> createResultMapForReason(
            String resultReason, List<TestCaseSummary> casesForReason) {

        List<List<TestCaseSummary>> clusters = bucketBySimilarity(casesForReason);

        Map<String, Object> result = new HashMap<>();
        result.put("resultReason", resultReason);
        result.put("buckets", createClusterMaps(clusters));
        result.put("status", casesForReason.getFirst().getResult());
        result.put("totalTests", clusters.stream().map(List::size).reduce(0, Integer::sum));
        return result;
    }

    private static List<List<TestCaseSummary>> bucketBySimilarity(List<TestCaseSummary> testCases) {
        final List<List<TestCaseSummary>> buckets = new ArrayList<>();
        final List<String> repBodies = new ArrayList<>(); // representatives (raw)
        final List<String> repNorms = new ArrayList<>(); // representatives (normalized)

        final Map<String, Integer> exactRepIdx = new HashMap<>();
        final Map<String, Integer> normRepIdx = new HashMap<>();
        final Map<String, String> normCache = new IdentityHashMap<>();

        int emptiesIdx = -1;

        for (TestCaseSummary tc : testCases) {
            final String body = tc.getResponseBody();

            // Route blanks into one bucket INSIDE the reason
            if (StringUtils.isBlank(body)) {
                if (emptiesIdx == -1) {
                    List<TestCaseSummary> b = new ArrayList<>();
                    b.add(tc);
                    buckets.add(b);
                    repBodies.add("");  // raw representative
                    repNorms.add("");   // normalized representative
                    emptiesIdx = buckets.size() - 1;
                } else {
                    buckets.get(emptiesIdx).add(tc);
                }
                continue;
            }

            // Fast path: exact raw equality with some bucket's representative
            Integer eq = exactRepIdx.get(body);
            if (eq != null) {
                buckets.get(eq).add(tc);
                continue;
            }

            // Compute normalized form once (generic: handles quoted & unquoted IDs)
            final String norm = normCache.computeIfAbsent(body, WordUtils::normalizeErrorMessage);

            // O(1) structural fast path: normalized string equality with any representative
            Integer nx = normRepIdx.get(norm);
            if (nx != null) {
                buckets.get(nx).add(tc);
                continue;
            }

            boolean placed = false;
            final int size = buckets.size();

            for (int i = 0; i < size; i++) {
                final String repRaw = repBodies.get(i);
                final String repNorm = repNorms.get(i);

                // exact raw equality (covers duplicates quickly)
                if (body.equals(repRaw)) {
                    buckets.get(i).add(tc);
                    exactRepIdx.put(body, i);
                    placed = true;
                    break;
                }

                // cheap Jaccard gate on normalized strings
                final double token = JS.apply(repNorm, norm);
                if (token < JACCARD_GATE) {
                    continue;
                }

                // expensive check with original predicate
                if (WordUtils.areErrorsSimilar(repRaw, body)) {
                    buckets.get(i).add(tc);
                    placed = true;
                    break;
                }
            }

            if (!placed) {
                List<TestCaseSummary> nb = new ArrayList<>(4);
                nb.add(tc);
                buckets.add(nb);
                repBodies.add(body);
                repNorms.add(norm);
                exactRepIdx.putIfAbsent(body, buckets.size() - 1);
                normRepIdx.putIfAbsent(norm, buckets.size() - 1);
            }
        }
        return buckets;
    }

    private static List<Map<String, Object>> createClusterMaps(List<List<TestCaseSummary>> clusters) {
        List<Map<String, Object>> bucketList = new ArrayList<>(clusters.size());
        int bucketCounter = 1;

        for (List<TestCaseSummary> bucket : clusters) {
            Map<String, Object> map = new HashMap<>();
            map.put("bucketId", bucketCounter++);

            String representativeError = bucket.getFirst().getResponseBody();
            if (StringUtils.isBlank(representativeError)) {
                representativeError = EMPTY_BODY_LABEL;
            }
            map.put("errorMessage", representativeError);
            map.put("borderColor", generateRandomHexColor());
            map.put("paths", createPathList(bucket));

            bucketList.add(map);
        }
        return bucketList;
    }

    private static List<Map<String, Object>> createPathList(List<TestCaseSummary> bucket) {
        Map<String, StringBuilder> pathGroups = new LinkedHashMap<>();

        for (TestCaseSummary tc : bucket) {
            String path = tc.getPath();
            StringBuilder sb = pathGroups.computeIfAbsent(path, k -> new StringBuilder());
            if (!sb.isEmpty()) sb.append(", ");
            sb.append("<a href=\"")
                    .append(tc.getKey())
                    .append(".html\" target=\"_blank\">")
                    .append(tc.getId())
                    .append("</a>");
        }

        List<Map<String, Object>> paths = new ArrayList<>(pathGroups.size());
        for (Map.Entry<String, StringBuilder> e : pathGroups.entrySet()) {
            Map<String, Object> pm = new HashMap<>();
            pm.put("path", e.getKey());
            pm.put("testCases", e.getValue().toString());
            paths.add(pm);
        }
        return paths;
    }

    private static String generateRandomHexColor() {
        int r = CommonUtils.random().nextInt(256);
        int g = CommonUtils.random().nextInt(256);
        int b = CommonUtils.random().nextInt(256);
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
