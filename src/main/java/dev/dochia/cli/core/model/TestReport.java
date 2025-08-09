package dev.dochia.cli.core.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents a report summarizing the results of tests cases execution.
 * This report includes information about the number of warnings, errors, and successful tests.
 */
@Getter
@Builder
public class TestReport {
    private final List<TestCaseSummary> testCases;
    private final int totalTests;
    private final int success;
    private final int warnings;
    private final int errors;
    private final long executionTime;
    private final String timestamp;
    private final String dochiaVersion;

    public List<JunitTestSuite> getTestSuites() {
        DecimalFormat decimalFormat = new DecimalFormat("#.###");

        Map<String, List<TestCaseSummary>> groupedByPlaybook = testCases.stream()
                .collect(Collectors.groupingBy(TestCaseSummary::getPlaybook));

        List<JunitTestSuite> junitTestSuites = new ArrayList<>();

        for (Map.Entry<String, List<TestCaseSummary>> entry : groupedByPlaybook.entrySet()) {
            String playbook = entry.getKey();
            List<TestCaseSummary> testCasesPerPlaybook = entry.getValue();

            // Compute testsuite-level details
            int totalTestsPerPlaybook = testCasesPerPlaybook.size();
            int failuresPerPlaybook = (int) testCasesPerPlaybook.parallelStream()
                    .filter(TestCaseSummary::getError)
                    .filter(Predicate.not(TestCaseSummary::is9xxResponse))
                    .count();
            int errorsPerPlaybook = (int) testCasesPerPlaybook.parallelStream()
                    .filter(TestCaseSummary::getError)
                    .filter(TestCaseSummary::is9xxResponse)
                    .count();
            int warningsPerPlaybook = (int) testCasesPerPlaybook.parallelStream().filter(TestCaseSummary::getWarning).count();
            double totalTime = testCasesPerPlaybook.parallelStream().mapToDouble(TestCaseSummary::getTimeToExecuteInSec).sum();

            // Create the testsuite object
            JunitTestSuite junitTestSuite = new JunitTestSuite();
            junitTestSuite.playbook = playbook;
            junitTestSuite.totalTests = totalTestsPerPlaybook;
            junitTestSuite.failures = failuresPerPlaybook;
            junitTestSuite.warnings = warningsPerPlaybook;
            junitTestSuite.errors = errorsPerPlaybook;
            junitTestSuite.time = decimalFormat.format(totalTime);
            junitTestSuite.testCases = testCasesPerPlaybook;

            junitTestSuites.add(junitTestSuite);
        }

        return junitTestSuites;
    }

    /**
     * Retrieves the number of failed tests. Failed tests are those that have an error and are not 9xx responses.
     *
     * @return The number of failed tests.
     */
    public int getFailuresJunit() {
        return (int) testCases.parallelStream()
                .filter(TestCaseSummary::getError)
                .filter(Predicate.not(TestCaseSummary::is9xxResponse))
                .count();
    }

    /**
     * Retrieves the number of errors. Errors are those that have an error and are 9xx responses.
     *
     * @return The number of failed tests.
     */
    public int getErrorsJunit() {
        return (int) testCases.parallelStream()
                .filter(TestCaseSummary::getError)
                .filter(TestCaseSummary::is9xxResponse)
                .count();
    }

    public static class JunitTestSuite {
        String playbook;
        int totalTests;
        int failures;
        int errors;
        int warnings;
        String time;
        List<TestCaseSummary> testCases;
    }
}
