package dev.dochia.cli.core.report;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import dev.dochia.cli.core.args.ReportingArguments;
import dev.dochia.cli.core.context.GlobalContext;
import dev.dochia.cli.core.model.*;
import dev.dochia.cli.core.model.ann.ExcludeTestCaseStrategy;
import dev.dochia.cli.core.playbook.api.DryRun;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.KeyValuePair;
import dev.dochia.cli.core.util.KeyValueSerializer;
import dev.dochia.cli.core.util.LongTypeSerializer;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.fusesource.jansi.Ansi;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * This class is responsible for writing the final report file(s).
 */

@Slf4j
public abstract class TestCaseExporter {
    static final String REPORT_HTML = "index.html";
    static final MustacheFactory mustacheFactory = new DefaultMustacheFactory();
    static final Mustache SUMMARY_MUSTACHE = mustacheFactory.compile("summary.mustache");
    private static final String HTML = ".html";
    private static final String JSON = ".json";
    private static final Mustache TEST_CASE_MUSTACHE = mustacheFactory.compile("test-case.mustache");
    private static final String STACKTRACE = "Stacktrace";
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(TestCaseExporter.class);

    final ReportingArguments reportingArguments;
    final GlobalContext globalContext;

    private Path reportingPath;
    private long t0;
    private final Gson maskingSerializer;
    private static final DecimalFormat LARGE_NUMBER_FORMAT;
    private static final DecimalFormat SINGLE_DECIMAL_FORMAT = new DecimalFormat("#0.0");
    private static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("#0.00");

    @Getter
    @ConfigProperty(name = "quarkus.application.version", defaultValue = "1.0.0")
    String appVersion;

    final String osDetails;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' '); // Set space as the grouping separator
        LARGE_NUMBER_FORMAT = new DecimalFormat("#,###", symbols);
    }


    /**
     * Constructs a new instance of TestCaseExporter with the specified reporting arguments.
     *
     * @param reportingArguments the reporting arguments for configuring the TestCaseExporter
     */
    @Inject
    protected TestCaseExporter(ReportingArguments reportingArguments, GlobalContext globalContext) {
        this.reportingArguments = reportingArguments;
        this.globalContext = globalContext;
        maskingSerializer = new GsonBuilder()
                .setStrictness(Strictness.LENIENT)
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .setExclusionStrategies(new ExcludeTestCaseStrategy())
                .registerTypeAdapter(Long.class, new LongTypeSerializer())
                .registerTypeAdapter(KeyValuePair.class, new KeyValueSerializer(reportingArguments.getMaskedHeaders()))
                .serializeNulls()
                .create();
        this.osDetails = System.getProperty("os.name") + "-" + System.getProperty("os.version") + "-" + System.getProperty("os.arch");
    }

    /**
     * Initializes the reporting path for the test reports.
     *
     * @param folder The custom output folder path. If not provided (or blank), the default folder from reporting arguments is used.
     * @throws IOException If an I/O error occurs during file or directory operations.
     */
    public void initPath(String folder) throws IOException {
        String outputFolder = reportingArguments.getOutputReportFolder();
        if (!StringUtils.isBlank(folder)) {
            outputFolder = folder;
        }
        String subFolder = reportingArguments.isTimestampReports() ? String.valueOf(System.currentTimeMillis()) : "";
        reportingPath = Paths.get(outputFolder, subFolder);

        if (!reportingArguments.isTimestampReports() && reportingPath.toFile().exists()) {
            deleteFiles(reportingPath);
        }
        if (!reportingPath.toFile().exists()) {
            Files.createDirectories(reportingPath);
        }

        t0 = System.currentTimeMillis();
    }

    private void deleteFiles(Path path) throws IOException {
        logger.debug("Start cleaning up dochia-report folder ...");
        File[] files = path.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    Files.delete(file.toPath());
                }
            }
        }
        logger.debug("Cleanup complete!");
    }

    /**
     * Writes number of errors encountered by reason.
     *
     * @param testCaseSummaryDetails a list of test case summaries
     */
    public void writeErrorsByReason(List<TestCaseSummary> testCaseSummaryDetails) {
        if (testCaseSummaryDetails == null || testCaseSummaryDetails.isEmpty()) {
            return;
        }
        Map<String, Long> resultReasonCounts = testCaseSummaryDetails.stream()
                .filter(testCase -> StringUtils.isNotBlank(testCase.getResultReason()))
                .filter(TestCaseSummary::getError)
                .collect(Collectors.groupingBy(TestCaseSummary::getResultReason, Collectors.counting()));

        if (resultReasonCounts.isEmpty()) {
            return;
        }
        String redCross = ansi().fgRed().a("✖").reset().toString();
        ConsoleUtils.emptyLine();
        logger.info("Errors found:");
        resultReasonCounts.forEach((reason, count) ->
                logger.noFormat(" {} {} ({} error(s))", redCross, reason, count));
    }

    /**
     * Writes performance statistics for the executed test cases, including execution time details.
     * The method checks if printing execution statistics is enabled in the reporting arguments before generating and printing the report.
     *
     * @param executionSummaries a map containing the summaries of executed test cases
     */
    public void writePerformanceReport(List<TestCaseExecutionSummary> executionSummaries) {
        if (reportingArguments.isPrintExecutionStatistics()) {
            Map<String, List<TestCaseExecutionSummary>> executionDetails = extractExecutionDetails(executionSummaries);

            ConsoleUtils.renderHeader(" Execution time details ");
            ConsoleUtils.emptyLine();
            executionDetails.forEach(this::writeExecutionTimesForPathAndHttpMethod);
        } else {
            ConsoleUtils.emptyLine();
            logger.info("Skip printing time execution statistics. You can use --printExecutionStatistics to enable this feature!");
        }
    }

    private Map<String, List<TestCaseExecutionSummary>> extractExecutionDetails(List<TestCaseExecutionSummary> summaries) {
        return summaries
                .stream()
                .collect(Collectors.groupingBy(testCase -> testCase.httpMethod() + " " + testCase.path()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void writeExecutionTimesForPathAndHttpMethod(String key, List<TestCaseExecutionSummary> value) {
        double average = value.stream().mapToLong(TestCaseExecutionSummary::responseTimeInMs).average().orElse(0);
        List<TestCaseExecutionSummary> sortedRuns = value.stream()
                .sorted(Comparator.comparingLong(TestCaseExecutionSummary::responseTimeInMs))
                .toList();

        TestCaseExecutionSummary bestCaseTestCase = sortedRuns.getFirst();
        TestCaseExecutionSummary worstCaseTestCase = sortedRuns.getLast();
        List<TimeExecution> executions = sortedRuns.stream()
                .map(tetCase -> TimeExecution.builder()
                        .testId(tetCase.testId())
                        .executionInMs(tetCase.responseTimeInMs())
                        .build())
                .toList();

        TimeExecutionDetails timeExecutionDetails = TimeExecutionDetails.builder().average(average)
                .path(key).bestCase(TimeExecution.builder()
                        .testId(bestCaseTestCase.testId())
                        .executionInMs(bestCaseTestCase.responseTimeInMs())
                        .build())
                .worstCase(TimeExecution.builder()
                        .testId(worstCaseTestCase.testId())
                        .executionInMs(worstCaseTestCase.responseTimeInMs())
                        .build())
                .executions(executions).build();


        logger.info("Details for path {} ", ansi().fg(Ansi.Color.GREEN).a(timeExecutionDetails.getPath()).reset());
        logger.timer(ansi().fgYellow().a("Average response time: {}ms").reset().toString(), ansi().bold().a(NumberFormat.getInstance().format(timeExecutionDetails.getAverage())));
        logger.timer(ansi().fgRed().a("Worst case response time: {}").reset().toString(), ansi().bold().a(timeExecutionDetails.getWorstCase().executionTimeString()));
        logger.timer(ansi().fgGreen().a("Best case response time: {}").reset().toString(), ansi().bold().a(timeExecutionDetails.getBestCase().executionTimeString()));
        ConsoleUtils.emptyLine();

        if (reportingArguments.isPrintDetailedExecutionStatistics()) {
            logger.timer("{} executed tests (sorted by response time):  {}", timeExecutionDetails.getExecutions().size(), timeExecutionDetails.getExecutions());
            logger.noFormat(" ");
        }
    }

    /**
     * Prints the execution details including the overall dochia execution time, the total number of requests, and statistics on passed, warnings, and errors.
     * It also provides a message with a link to the generated report if available.
     *
     * @param executionStatisticsListener the listener providing statistics on dochia execution
     */
    public void printExecutionDetails(ExecutionStatisticsListener executionStatisticsListener) {
        String dochiaFinished = ansi().fgBlue().a("{} tests completed in {}\n").toString();
        String passed = ansi().fgGreen().bold().a("  ✔ {} passed, ").toString();
        String warnings = ansi().fgYellow().bold().a("⚠ {} warnings, ").toString();
        String errors = ansi().fgRed().bold().a("‼ {} errors").toString();
        String check = ansi().reset().fgBlue().a(String.format("Full Report: %s ", reportingPath.toUri() + getSummaryReportTitle())).reset().toString();
        String finalMessage = dochiaFinished + passed + warnings + errors;
        String duration = Duration.ofMillis(System.currentTimeMillis() - t0).toString().toLowerCase(Locale.ROOT).substring(2);

        ConsoleUtils.emptyLine();
        logger.complete(finalMessage, executionStatisticsListener.getAll(), duration, executionStatisticsListener.getSuccess(), executionStatisticsListener.getWarns(), executionStatisticsListener.getErrors(), executionStatisticsListener.getSkipped());
        ConsoleUtils.emptyLine();
        logger.complete(check);
    }


    /**
     * Writes a summary report based on the provided test case map and execution statistics.
     * It creates a TestReport and extracts information such as warnings, success, errors, and total tests.
     * The gathered information is stored in a context map.
     *
     * @param summaries                   the pre-created summary for each test case
     * @param executionStatisticsListener the listener providing statistics on dochia execution
     */
    public void writeSummary(List<TestCaseSummary> summaries, ExecutionStatisticsListener executionStatisticsListener) {
        TestReport report = this.createTestReport(summaries, executionStatisticsListener);
        double averageResponseTime = summaries.stream().mapToDouble(TestCaseSummary::getTimeToExecuteInMs).sum() / summaries.size();

        Map<String, Object> context = new HashMap<>();
        context.put("WARNINGS", LARGE_NUMBER_FORMAT.format(report.getWarnings()));
        context.put("SUCCESS", LARGE_NUMBER_FORMAT.format(report.getSuccess()));
        context.put("ERRORS", LARGE_NUMBER_FORMAT.format(report.getErrors()));
        context.put("ERRORS_JUNIT", LARGE_NUMBER_FORMAT.format(report.getErrorsJunit()));
        context.put("FAILURES_JUNIT", LARGE_NUMBER_FORMAT.format(report.getFailuresJunit()));
        context.put("TOTAL", LARGE_NUMBER_FORMAT.format(report.getTotalTests()));
        context.put("TIMESTAMP", report.getTimestamp());
        context.put("TIMESTAMP_ISO", OffsetDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME));
        context.put("TEST_CASES", report.getTestCases());
        context.put("TEST_SUITES", report.getTestSuites());
        context.put("EXECUTION", Duration.ofSeconds(report.getExecutionTime()).toString().toLowerCase(Locale.ROOT).substring(2));
        context.put("TIME", report.getExecutionTime());
        context.put("VERSION", report.getDochiaVersion());
        context.put("JS", this.isJavascript());
        context.put("OS", this.osDetails);
        context.put("AVERAGE_RESPONSE_TIME", SINGLE_DECIMAL_FORMAT.format(averageResponseTime));

        String warnPercentage = DOUBLE_DECIMAL_FORMAT.format((double) report.getWarnings() / report.getTotalTests() * 100);
        String errorPercentage = DOUBLE_DECIMAL_FORMAT.format((double) report.getErrors() / report.getTotalTests() * 100);
        String successPercentage = DOUBLE_DECIMAL_FORMAT.format((double) report.getSuccess() / report.getTotalTests() * 100);

        context.put("WARN_PERCENTAGE", warnPercentage);
        context.put("ERROR_PERCENTAGE", errorPercentage);
        context.put("SUCCESS_PERCENTAGE", successPercentage);

        DochiaConfiguration dochiaConfiguration = globalContext.getDochiaConfiguration();

        if (dochiaConfiguration != null) {
            context.put("CONTRACT_NAME", dochiaConfiguration.contract());
            context.put("BASE_URL", dochiaConfiguration.basePath());
            context.put("HTTP_METHODS", dochiaConfiguration.httpMethods().stream().map(Enum::name).map(String::toLowerCase).toList());
            context.put("PLAYBOOKS", dochiaConfiguration.playbooks());
            context.put("TOTAL_PLAYBOOKS", dochiaConfiguration.totalPlaybooks());
            context.put("PATHS", dochiaConfiguration.pathsToRun());
            context.put("TOTAL_PATHS", dochiaConfiguration.totalPaths());
        }

        var groupedTestCases = BucketsCalculator.createBuckets(summaries);
        context.put("GROUPED_TEST_CASES", groupedTestCases);

        Writer writer = this.getSummaryTemplate().execute(new StringWriter(), context);

        try {
            writer.flush();
            String content = writer.toString();
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            Files.write(Paths.get(reportingPath.toFile().getAbsolutePath(), this.getSummaryReportTitle()),
                    bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            logger.error("There was an error writing the report summary: {}. Please check if dochia has proper right to write in the report location: {}",
                    e.getMessage(), reportingPath.toFile().getAbsolutePath());
            logger.debug(STACKTRACE, e);
        }

    }

    private TestReport createTestReport(List<TestCaseSummary> summaries, ExecutionStatisticsListener executionStatisticsListener) {
        List<TestCaseSummary> sortedSummaries = summaries.stream().sorted().toList();

        return TestReport.builder().testCases(sortedSummaries).errors(executionStatisticsListener.getErrors())
                .success(executionStatisticsListener.getSuccess()).totalTests(executionStatisticsListener.getAll())
                .warnings(executionStatisticsListener.getWarns()).timestamp(OffsetDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME))
                .executionTime(((System.currentTimeMillis() - t0) / 1000))
                .dochiaVersion(appVersion).build();
    }

    /**
     * Writes helper files, such as assets and specific files, to the reporting path.
     * It includes assets and copies specific helper files from the classpath to the reporting path.
     * The specific helper files are determined by the implementation of getSpecificHelperFiles method.
     */
    public void writeHelperFiles() {
        try {
            writeAssets();
            for (String file : this.getSpecificHelperFiles()) {
                try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(file)) {
                    Files.copy(Objects.requireNonNull(stream), Paths.get(reportingPath.toFile().getAbsolutePath(), file));
                }
            }
        } catch (IOException e) {
            logger.error("Unable to write reporting files: {}. Please check if dochia has proper right to write in the report location: {}",
                    e.getMessage(), reportingPath.toFile().getAbsolutePath());
            logger.debug(STACKTRACE, e);
        }
    }

    /**
     * Writes assets to the reporting path. Assets are stored in a directory named "assets"
     * within the reporting path. It creates the "assets" directory if it doesn't exist.
     *
     * @throws IOException if an I/O error occurs while creating directories
     */
    public void writeAssets() throws IOException {
        Path assetsPath = Paths.get(reportingPath.toFile().getAbsolutePath(), "assets");
        Files.createDirectories(assetsPath);

        try (ZipInputStream zis = new ZipInputStream(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("assets.zip")))) {
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                Files.copy(zis, Paths.get(assetsPath.toFile().getAbsolutePath(), zipEntry.getName()), StandardCopyOption.REPLACE_EXISTING);
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    /**
     * We mark it as DryRun in order to avoid writing test cases when in dryRun mode.
     *
     * @param testCase the current test case
     */
    @DryRun
    public void writeTestCase(TestCase testCase) {
        writeHtmlTestCase(testCase);
        writeJsonTestCase(testCase);
    }

    private void writeJsonTestCase(TestCase testCase) {
        String testFileName = testCase.getTestId().replace(" ", "").concat(JSON);
        try {
            Files.writeString(Paths.get(reportingPath.toFile().getAbsolutePath(), testFileName), maskingSerializer.toJson(testCase), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("There was a problem writing test case {}: {}. Please check if dochia has proper right to write in the report location: {}",
                    testCase.getTestId(), e.getMessage(), reportingPath.toFile().getAbsolutePath());
            logger.debug(STACKTRACE, e);
        }
    }

    private void writeHtmlTestCase(TestCase testCase) {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> context = new HashMap<>();
        testCase.setJs(this.isJavascript());
        testCase.setMaskingSerializer(maskingSerializer);
        context.put("TEST_CASE", testCase);
        context.put("TIMESTAMP", OffsetDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME));
        context.put("VERSION", appVersion);
        context.put("JS", this.isJavascript());
        Writer writer = TEST_CASE_MUSTACHE.execute(stringWriter, context);
        String testFileName = testCase.getTestId().replace(" ", "").concat(HTML);
        try {
            Files.writeString(Paths.get(reportingPath.toFile().getAbsolutePath(), testFileName), writer.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("There was a problem writing test case {}: {}. Please check if dochia has proper right to write in the report location: {}",
                    testCase.getTestId(), e.getMessage(), reportingPath.toFile().getAbsolutePath());
            logger.debug(STACKTRACE, e);
        } catch (NegativeArraySizeException e) {
            logger.debug(e.getMessage());
            logger.debug(STACKTRACE, e);
        }
    }


    /**
     * Indicates whether the report format involves JavaScript functionality.
     *
     * @return {@code true} if the report format requires JavaScript, {@code false} otherwise.
     */
    protected boolean isJavascript() {
        return false;
    }

    /**
     * Retrieves an array of specific helper files required for the reporting format.
     *
     * @return An array of file names representing the specific helper files.
     */
    public abstract String[] getSpecificHelperFiles();

    /**
     * Retrieves the report format associated with the exporter.
     *
     * @return The report format of the exporter.
     */
    public abstract ReportingArguments.ReportFormat reportFormat();

    /**
     * Retrieves the Mustache template used for generating the summary section of the report.
     *
     * @return The Mustache template for the summary section.
     */
    public abstract Mustache getSummaryTemplate();

    /**
     * Retrieves the title for the summary report.
     *
     * @return The title for the summary report.
     */
    public abstract String getSummaryReportTitle();
}
