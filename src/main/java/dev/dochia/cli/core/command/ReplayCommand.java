package dev.dochia.cli.core.command;

import dev.dochia.cli.core.args.AuthArguments;
import dev.dochia.cli.core.command.model.HelpFullOption;
import dev.dochia.cli.core.dsl.DSLParser;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.TestCase;
import dev.dochia.cli.core.report.TestCaseExporter;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.util.JsonUtils;
import dev.dochia.cli.core.util.KeyValuePair;
import dev.dochia.cli.core.util.VersionProvider;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.quarkus.arc.Unremovable;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This will replay a given list of tests solely based on the information received in the test case file(s).
 */
@CommandLine.Command(
        name = "replay",
        mixinStandardHelpOptions = true,
        usageHelpAutoWidth = true,
        description = "Replay previously executed dochia tests.",
        exitCodeListHeading = "%n@|bold,underline Exit Codes:|@%n",
        exitCodeList = {"@|bold  0|@:Successful program execution",
                "@|bold 2|@:Usage error: user input for the command was incorrect",
                "@|bold 1|@:Internal execution error: an exception occurred when executing command"},
        footerHeading = "%n@|bold,underline Examples:|@%n",
        footer = {"  Replay Test 1 from the default reporting folder:",
                "    dochia replay Test1",
                "", "  Replay Test 1 from the default reporting folder and write the new output in another folder",
                "    dochia replay Test1 --output path/to/new/folder",
                "", "  Retry all failed (error) tests from the default dochia-report folder:",
                "    dochia replay --errors",
                "", "  Retry all failed tests including warnings:",
                "    dochia replay --errors --warnings"},
        versionProvider = VersionProvider.class)
@Unremovable
public class ReplayCommand implements Runnable {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(ReplayCommand.class);
    private final ServiceCaller serviceCaller;
    private final TestCaseListener testCaseListener;

    @CommandLine.Parameters(
            description = "The list of tests. When providing a .json extension it will be considered a path, " +
                    "otherwise it will look for that test in the dochia-report folder", split = ",", arity = "1..")
    String[] tests;

    @Inject
    @CommandLine.ArgGroup(heading = "%n@|bold,underline Authentication Options:|@%n", exclusive = false)
    AuthArguments authArgs;

    @CommandLine.Mixin
    HelpFullOption helpFullOption;

    @CommandLine.Option(names = {"-v", "--verbose"},
            description = "Prints verbose information about the execution")
    private boolean verbose;

    @CommandLine.Option(names = {"-H"},
            description = "Specifies the headers to be passed with all the re-played tests. It will override values from the replay files for the same header name")
    Map<String, Object> headersMap = new HashMap<>();

    @CommandLine.Option(names = {"-s", "--server"},
            description = "Base URL of the service. It can be used to overwrite the base URL from the initial test in order to replay it against other service instances")
    private String server;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "If supplied, it will create TestXXX.json files within the given folder with the updated responses received when replaying the tests")
    private String outputReportFolder;

    @CommandLine.Option(names = {"--errors"},
            description = "Retry all tests with error results from the dochia-summary-report.json in the report folder")
    private boolean errors;

    @CommandLine.Option(names = {"--warnings"},
            description = "Retry all tests with warning results from the dochia-summary-report.json in the report folder")
    private boolean warnings;

    @CommandLine.Option(names = {"--report-folder", "-r"},
            description = "The folder containing the dochia-summary-report.json file when using --errors/--warnings. Default: @|bold,underline dochia-report|@")
    private String reportFolder = "dochia-report";


    /**
     * Constructs a new instance of the {@code ReplayCommand} class.
     *
     * @param serviceCaller    the service caller used for invoking services during replay
     * @param testCaseListener the test case listener used for handling test case events during replay
     */
    @Inject
    public ReplayCommand(ServiceCaller serviceCaller, TestCaseListener testCaseListener) {
        this.serviceCaller = serviceCaller;
        this.testCaseListener = testCaseListener;
    }

    private List<String> parseTestCases() {
        List<String> testCaseFiles = new ArrayList<>();

        // Add tests from retry options (--errors, --warnings)
        if (errors || warnings) {
            testCaseFiles.addAll(loadTestIdsFromSummaryReport());
        }

        // Add explicitly provided test cases
        if (tests != null && tests.length > 0) {
            testCaseFiles.addAll(Arrays.stream(tests)
                    .map(testCase -> testCase.trim().strip())
                    .map(testCase -> testCase.endsWith(".json") ? testCase : reportFolder + "/" + testCase + ".json")
                    .toList());
        }

        return testCaseFiles;
    }

    private List<String> loadTestIdsFromSummaryReport() {
        Path summaryPath = Paths.get(reportFolder, TestCaseExporter.REPORT_JS);
        if (!Files.exists(summaryPath)) {
            logger.error("Summary report not found at: {}", summaryPath);
            return Collections.emptyList();
        }

        try {
            String content = Files.readString(summaryPath);
            SummaryReport report = JsonUtils.GSON.fromJson(content, SummaryReport.class);

            if (report == null || report.testCases == null) {
                logger.error("Invalid summary report format");
                return Collections.emptyList();
            }

            List<String> failedIds = new ArrayList<>();
            for (TestCaseSummaryEntry entry : report.testCases) {
                if (shouldRetryTest(entry)) {
                    String testId = entry.id.replace(" ", "");
                    failedIds.add(reportFolder + "/" + testId + ".json");
                }
            }

            if (failedIds.isEmpty()) {
                logger.info("No failed tests found to retry");
            } else {
                logger.info("Found {} failed test(s) to retry", failedIds.size());
            }

            return failedIds;
        } catch (IOException e) {
            logger.error("Failed to read summary report: {}", e.getMessage());
            logger.debug("Stacktrace:", e);
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Failed to parse summary report: {}", e.getMessage());
            logger.debug("Stacktrace:", e);
            return Collections.emptyList();
        }
    }

    private boolean shouldRetryTest(TestCaseSummaryEntry entry) {
        if (entry.result == null) {
            return false;
        }
        boolean isError = errors && "error".equalsIgnoreCase(entry.result);
        boolean isWarning = warnings && "warn".equalsIgnoreCase(entry.result);
        return isError || isWarning;
    }

    /**
     * Internal class for deserializing the summary report.
     */
    static class SummaryReport {
        List<TestCaseSummaryEntry> testCases;
    }

    /**
     * Internal class for deserializing individual test case entries from the summary.
     */
    static class TestCaseSummaryEntry {
        String id;
        String result;
    }

    /**
     * Tracks replay statistics for summary display.
     */
    static class ReplayStats {
        int initialErrors;
        int initialWarnings;
        int unchanged;
        int improved;
        int regressed;
    }

    private void executeTestCase(String testCaseFileName, ReplayStats stats) throws IOException {
        TestCase testCase = this.loadTestCaseFile(testCaseFileName);
        logger.start("Calling service endpoint: {}", testCase.getRequest().getUrl());
        this.loadHeadersIfSupplied(testCase);

        HttpResponse response;
        try {
            response = serviceCaller.callService(testCase.getRequest(), Collections.emptySet());
        } catch (IOException e) {
            HttpResponse.ExceptionalResponse exceptionalResponse = HttpResponse.getResponseByException(e);
            response = HttpResponse.builder()
                    .jsonBody(JsonUtils.parseAsJsonElement(exceptionalResponse.responseBody()))
                    .body(exceptionalResponse.responseBody())
                    .responseCode(exceptionalResponse.responseCode())
                    .build();
        }

        if (verbose) {
            logger.complete("Response body: \n{}", response.getBody());
        }
        this.writeTestJsonsIfSupplied(testCase, response);
        this.showResponseCodesDifferences(testCase, response);
        this.updateStats(testCase, response, stats);
    }

    private void updateStats(TestCase testCase, HttpResponse response, ReplayStats stats) {
        if (stats == null) {
            return;
        }
        int oldCode = testCase.getResponse().getResponseCode();
        int newCode = response.getResponseCode();

        boolean wasError = oldCode >= 500 || oldCode == 0;
        boolean isNowError = newCode >= 500 || newCode == 0;
        boolean isNowClientError = newCode >= 400 && newCode < 500;
        boolean isNowSuccess = newCode >= 200 && newCode < 300;

        if (oldCode == newCode) {
            stats.unchanged++;
        } else if (isNowSuccess || (wasError && isNowClientError)) {
            stats.improved++;
        } else if (isNowError) {
            stats.regressed++;
        }
    }

    void showResponseCodesDifferences(TestCase testCase, HttpResponse response) {
        logger.noFormat("");
        logger.star("Old response code: {}", testCase.getResponse().getResponseCode());
        logger.star("New response code: {}", response.getResponseCode());
        logger.noFormat("");

        if (verbose) {
            logger.star("Old response body: {}", testCase.getResponse().getJsonBody());
            logger.star("New response body: {}", response.getJsonBody());
            logger.noFormat("");
        }
    }

    void writeTestJsonsIfSupplied(TestCase testCase, HttpResponse response) {
        if (StringUtils.isBlank(this.outputReportFolder)) {
            return;
        }

        testCase.setResponse(response);
        testCaseListener.writeIndividualTestCase(testCase);
    }

    private void loadHeadersIfSupplied(TestCase testCase) {
        List<KeyValuePair<String, Object>> headersFromFile = new java.util.ArrayList<>(Optional.ofNullable(testCase.getRequest().getHeaders()).orElse(Collections.emptyList()));

        headersFromFile.removeIf(header -> headersMap.containsKey(header.getKey()));
        headersFromFile.addAll(headersMap.entrySet().stream().map(entry -> new KeyValuePair<>(entry.getKey(), entry.getValue())).toList());

        headersFromFile.forEach(header -> header.setValue(DSLParser.parseAndGetResult(header.getValue().toString(), authArgs.getAuthScriptAsMap())));
    }

    private TestCase loadTestCaseFile(String testCaseFileName) throws IOException {
        String testCaseFile = Files.readString(Paths.get(testCaseFileName));
        if (verbose) {
            logger.config("Loaded content: \n" + testCaseFile);
        }
        TestCase testCase = JsonUtils.GSON.fromJson(testCaseFile, TestCase.class);
        testCase.updateServer(server);
        return testCase;
    }

    private void initReportingPath() {
        if (StringUtils.isBlank(this.outputReportFolder)) {
            return;
        }

        try {
            testCaseListener.initReportingPath(this.outputReportFolder);
            testCaseListener.writeHelperFiles();
        } catch (IOException e) {
            logger.error("There was an issue creating the output folder: {}", e.getMessage());
            logger.debug("Stacktrace:", e);
        }
    }


    @Override
    public void run() {
        List<String> testCases = this.parseTestCases();
        if (testCases.isEmpty()) {
            logger.warning("No tests to replay. Provide test names as arguments or use --errors/--warnings");
            return;
        }

        this.initReportingPath();
        ReplayStats stats = (errors || warnings) ? createInitialStats() : null;

        for (String testCaseFileName : testCases) {
            try {
                logger.noFormat("");
                logger.start("Executing {}", testCaseFileName);
                this.executeTestCase(testCaseFileName, stats);
                logger.complete("Finish executing {}", testCaseFileName);
            } catch (IOException e) {
                logger.debug("Exception while replaying test!", e);
                logger.error("Something went wrong while replaying {}. If the test name ends with .json it is searched as a full path. " +
                        "If it doesn't have an extension it will be searched in the {} folder. Error message: {}", testCaseFileName, reportFolder, e.toString());
            }
        }

        if (stats != null) {
            printSummary(stats, testCases.size());
        }
    }

    private ReplayStats createInitialStats() {
        ReplayStats stats = new ReplayStats();
        Path summaryPath = Paths.get(reportFolder, TestCaseExporter.REPORT_JS);
        try {
            String content = Files.readString(summaryPath);
            SummaryReport report = JsonUtils.GSON.fromJson(content, SummaryReport.class);
            if (report != null && report.testCases != null) {
                for (TestCaseSummaryEntry entry : report.testCases) {
                    if ("error".equalsIgnoreCase(entry.result)) {
                        stats.initialErrors++;
                    } else if ("warn".equalsIgnoreCase(entry.result)) {
                        stats.initialWarnings++;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not read initial stats: {}", e.getMessage());
        }
        return stats;
    }

    private void printSummary(ReplayStats stats, int totalReplayed) {
        logger.noFormat("");
        logger.noFormat("─".repeat(60));
        logger.info("Replay Summary");
        logger.noFormat("─".repeat(60));
        logger.star("Total tests replayed: {}", totalReplayed);
        logger.star("Initial errors in report: {}", stats.initialErrors);
        logger.star("Initial warnings in report: {}", stats.initialWarnings);
        logger.noFormat("");
        logger.star("Unchanged (same response code): {}", stats.unchanged);
        logger.complete("Improved (better response): {}", stats.improved);
        if (stats.regressed > 0) {
            logger.error("Regressed (worse response): {}", stats.regressed);
        } else {
            logger.star("Regressed (worse response): {}", stats.regressed);
        }
        logger.noFormat("─".repeat(60));
    }
}
