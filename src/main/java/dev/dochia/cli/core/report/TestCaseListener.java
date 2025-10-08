package dev.dochia.cli.core.report;

import com.google.common.collect.Iterators;
import com.google.common.net.MediaType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.dochia.cli.core.args.IgnoreArguments;
import dev.dochia.cli.core.args.ReportingArguments;
import dev.dochia.cli.core.context.GlobalContext;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyDynamic;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.*;
import dev.dochia.cli.core.playbook.api.DryRun;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.WordUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Builder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.fusesource.jansi.Ansi;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.dochia.cli.core.context.GlobalContext.CONTRACT_PATH;
import static dev.dochia.cli.core.context.GlobalContext.HTTP_METHOD;
import static dev.dochia.cli.core.model.TestCase.SKIP_REPORTING;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * This class exposes methods to record the progress of a test case
 */
@ApplicationScoped
@DryRun
public class TestCaseListener {
    private static final Iterator<Character> cycle = Iterators.cycle('\\', '\\', '\\', '|', '|', '|', '/', '/', '/', '-', '-', '-');
    private static final String DEFAULT = "*******";
    static final String ID = "id";
    private static final String PLAYBOOK_KEY = "playbookKey";
    private static final String PLAYBOOK = "playbook";
    private static final String ID_ANSI = "id_ansi";
    static final AtomicInteger TEST = new AtomicInteger(0);
    private static final List<String> NOT_NECESSARILY_DOCUMENTED = Arrays.asList("406", "415", "414", "501", "413", "431");
    private static final String RECEIVED_RESPONSE_IS_MARKED_AS_IGNORED_SKIPPING = "Received response is marked as ignored... skipping!";
    private static final List<String> CONTENT_TYPE_DONT_MATCH_SCHEMA = List.of("application/csv", "application/pdf");
    final Map<String, TestCase> testCaseMap = new HashMap<>();
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(TestCaseListener.class);
    private static final String SEPARATOR = "-".repeat(ConsoleUtils.getConsoleColumns(22));
    private final ExecutionStatisticsListener executionStatisticsListener;
    private final TestReportsGenerator testReportsGenerator;
    private final GlobalContext globalContext;
    private final IgnoreArguments ignoreArguments;
    private final ReportingArguments reportingArguments;
    final List<TestCaseSummary> testCaseSummaryDetails = new ArrayList<>();
    final List<TestCaseExecutionSummary> testCaseExecutionDetails = new ArrayList<>();

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "1.0.0")
    String appVersion;
    @ConfigProperty(name = "quarkus.application.name", defaultValue = "dochia")
    String appName;
    @ConfigProperty(name = "app.timestamp", defaultValue = "1-1-1")
    String appBuildTime;

    private final Deque<String> runPerPathListener = new ArrayDeque<>();

    /**
     * Constructs a TestCaseListener with the provided dependencies and configuration.
     *
     * @param globalContext        the global context
     * @param er                   the listener for execution statistics
     * @param testReportsGenerator the generator for test reports
     * @param filterArguments      the arguments for filtering test cases
     * @param reportingArguments   the arguments for reporting test cases
     * @throws NoSuchElementException if no matching exporter is found for the specified report format
     */
    public TestCaseListener(GlobalContext globalContext, ExecutionStatisticsListener er, TestReportsGenerator testReportsGenerator, IgnoreArguments filterArguments, ReportingArguments reportingArguments) {
        this.executionStatisticsListener = er;
        this.testReportsGenerator = testReportsGenerator;
        this.ignoreArguments = filterArguments;
        this.globalContext = globalContext;
        this.reportingArguments = reportingArguments;
    }

    private static String replaceBrackets(String message, Object... params) {
        for (Object obj : params) {
            message = message.replaceFirst(Pattern.quote("{}"), Matcher.quoteReplacement(String.valueOf(obj)));
        }
        return message;
    }

    private String getKeyDefault() {
        return reportingArguments.isSummaryInConsole() ? "" : DEFAULT;
    }

    /**
     * Performs setup actions before fuzzing for the specified playbook class.
     *
     * @param playbook the class representing the playbook
     */
    public void beforeFuzz(Class<?> playbook, String path, String httpMethod) {
        String clazz = ConsoleUtils.removeTrimSanitize(playbook.getSimpleName()).replaceAll("[a-z]", "");
        MDC.put(PLAYBOOK, ConsoleUtils.centerWithAnsiColor(clazz, getKeyDefault().length(), Ansi.Color.MAGENTA));
        MDC.put(PLAYBOOK_KEY, ConsoleUtils.removeTrimSanitize(playbook.getSimpleName()));
        MDC.put(CONTRACT_PATH, path);
        MDC.put(HTTP_METHOD, httpMethod);
        this.notifySummaryObservers(path);
    }

    /**
     * Performs cleanup actions after fuzzing for a specific path and HTTP method.
     *
     * @param path the path for which fuzzing has been completed
     */
    public void afterFuzz(String path) {
        this.notifySummaryObservers(path);

        MDC.put(PLAYBOOK, this.getKeyDefault());
        MDC.put(PLAYBOOK_KEY, this.getKeyDefault());
        MDC.remove(CONTRACT_PATH);
        MDC.remove(HTTP_METHOD);
    }

    /**
     * Creates and executes a test by running the provided runnable.
     * Logs test start, catches exceptions during execution, logs results, and performs necessary cleanup.
     *
     * @param externalLogger   the external logger for logging test-related information
     * @param testCasePlaybook the playbook associated with the test
     * @param s                the runnable representing the test logic
     */
    public void createAndExecuteTest(PrettyLogger externalLogger, TestCasePlaybook testCasePlaybook, Runnable s, PlaybookData data) {
        this.startTestCase(data);
        try {
            s.run();
        } catch (Exception e) {
            ResultFactory.Result result = ResultFactory.createUnexpectedException(testCasePlaybook.getClass().getSimpleName(), Optional.ofNullable(e.getMessage()).orElse(""));
            this.reportResultError(externalLogger, data, result.reason(), result.message());
            externalLogger.error("Exception while processing: {}", e.getMessage());
            externalLogger.debug("Detailed stacktrace", e);
            this.checkForIOErrors(e);
        }
        this.endTestCase();
    }

    /**
     * Returns the current name of the playbook being executed.
     *
     * @return the playbook name that is currently being run
     */
    public String getCurrentPlaybook() {
        return MDC.get(PLAYBOOK_KEY);
    }

    /**
     * Returns the current test case number being executed.
     *
     * @return the test case number being executed
     */
    public int getCurrentTestCaseNumber() {
        return TEST.get();
    }

    /**
     * Returns the current test case identifier being executed. The test case identifier is a UUID
     * that can be used to trace the test case in service logs.
     *
     * @return the test case identifier being executed
     */
    public String getTestIdentifier() {
        return currentTestCase().getTraceId();
    }

    private void startTestCase(PlaybookData data) {
        String testId = String.valueOf(TEST.incrementAndGet());
        MDC.put(ID, testId);
        MDC.put(ID_ANSI, ConsoleUtils.centerWithAnsiColor(testId, 7, Ansi.Color.MAGENTA));

        TestCase testCase = new TestCase();
        testCase.setTestId("Test " + testId);
        testCase.setContractPath(data.getContractPath());
        testCase.setPath(data.getContractPath());
        testCase.getRequest().setHttpMethod(String.valueOf(data.getMethod()));
        testCaseMap.put(testId, testCase);
    }

    /**
     * Adds a scenario to the test case and logs it using the provided logger.
     *
     * @param logger   the logger used to log the scenario
     * @param scenario the scenario description template
     * @param params   the parameters to replace placeholders in the scenario description
     */
    public void addScenario(PrettyLogger logger, String scenario, Object... params) {
        logger.info(scenario, params);
        currentTestCase().setScenario(replaceBrackets(scenario, params));
    }

    /**
     * Adds an expected result to the test case and logs it using the provided logger.
     *
     * @param logger         the logger used to log the expected result
     * @param expectedResult the expected result description template
     * @param params         the parameters to replace placeholders in the expected result description
     */
    public void addExpectedResult(PrettyLogger logger, String expectedResult, Object... params) {
        logger.note(expectedResult, params);
        currentTestCase().setExpectedResult(replaceBrackets(expectedResult, params));
    }

    /**
     * Adds the specified path information to the current test case in the test case map.
     * The path is associated with the ongoing test case using the Mapped Diagnostic Context (MDC) identifier.
     *
     * @param path the path to be associated with the current test case
     */
    public void addPath(String path) {
        currentTestCase().setPath(path);
    }

    /**
     * Adds the specified contract path information to the current test case in the test case map.
     * The contract path is associated with the ongoing test case using the Mapped Diagnostic Context (MDC) identifier.
     *
     * @param path the contract path to be associated with the current test case
     */
    public void addContractPath(String path) {
        currentTestCase().setContractPath(path);
    }

    /**
     * Adds the specified server information to the current test case in the test case map.
     * The server information is associated with the ongoing test case using the Mapped Diagnostic Context (MDC) identifier.
     *
     * @param server the server information to be associated with the current test case
     */
    public void addServer(String server) {
        currentTestCase().setServer(server);
    }

    /**
     * Marks if the json payload is well-formed or not. This is useful for reporting display purposes,
     * to know if the json payload was valid or not.
     *
     * @param validJson true if the json is well-formed, false otherwise
     */
    public void addValidJson(boolean validJson) {
        currentTestCase().setValidJson(validJson);
    }

    /**
     * Adds the specified HttpRequest to the current test case in the test case map.
     * The HttpRequest is associated with the ongoing test case using the Mapped Diagnostic Context (MDC) identifier.
     *
     * @param request the HttpRequest to be associated with the current test case
     */
    public void addRequest(HttpRequest request) {
        currentTestCase().setRequest(request);
    }

    /**
     * Adds the specified HttpResponse to the current test case in the test case map.
     * The HttpResponse is associated with the ongoing test case using the Mapped Diagnostic Context (MDC) identifier.
     *
     * @param response the HttpResponse to be associated with the current test case
     */
    public void addResponse(HttpResponse response) {
        currentTestCase().setResponse(response);
        extractErrorLeaks();
    }

    /**
     * Adds the specified full request path information to the current test case in the test case map.
     * The full request path is associated with the ongoing test case using the Mapped Diagnostic Context (MDC) identifier.
     *
     * @param fullRequestPath the full request path to be associated with the current test case
     */
    public void addFullRequestPath(String fullRequestPath) {
        currentTestCase().setFullRequestPath(fullRequestPath);
    }

    private void endTestCase() {
        TestCase currentTestCase = currentTestCase();
        currentTestCase.setPlaybook(MDC.get(PLAYBOOK_KEY));
        if (currentTestCase.isNotSkipped()) {
            testReportsGenerator.writeTestCase(currentTestCase);
            keepSummary(currentTestCase);
        }
        keepExecutionDetails(currentTestCase);
        testCaseMap.remove(MDC.get(ID));
        MDC.remove(ID);
        MDC.put(ID_ANSI, this.getKeyDefault());
        logger.info(SEPARATOR);
    }

    private void keepSummary(TestCase testCase) {
        testCaseSummaryDetails.add(TestCaseSummary.fromTestCase(testCase));
    }

    private void keepExecutionDetails(TestCase testCase) {
        if (testCase.notIgnoredForExecutionStatistics() && reportingArguments.isPrintExecutionStatistics()) {
            testCaseExecutionDetails.add(new TestCaseExecutionSummary(testCase.getTestId(), testCase.getPath(),
                    testCase.getHttpMethod(), testCase.getResponse().getResponseTimeInMs()));
        }
    }

    /**
     * Notifies summary observers about the progress of a specific path and HTTP method during the testing session.
     * If configured to display summaries in the console, this method renders the progress dynamically.
     *
     * @param path the path for which the progress is being reported
     */
    public void notifySummaryObservers(String path) {
        if (!reportingArguments.isSummaryInConsole()) {
            return;
        }
        String playbook = MDC.get(PLAYBOOK);
        String prefix = ansi().fgBlue().a("(" + runPerPathListener.size() + "/" + globalContext.getDochiaConfiguration().pathsToRun() + ") ").fgDefault().toString();
        String printPath = prefix + path + " " + playbook + ConsoleUtils.SEPARATOR + executionStatisticsListener.resultAsStringPerPath(path);

        if (runPerPathListener.contains(path)) {
            ConsoleUtils.renderSameRow(printPath, cycle.next());
        } else {
            this.markPreviousPathAsDone();
            runPerPathListener.push(path);
            ConsoleUtils.renderNewRow(printPath, cycle.next());
        }
    }

    private void markPreviousPathAsDone() {
        String previousPath = runPerPathListener.peek();
        if (previousPath != null) {
            String toRenderPreviousPath = previousPath + ConsoleUtils.SEPARATOR + executionStatisticsListener.resultAsStringPerPath(previousPath);
            ConsoleUtils.renderSameRow(toRenderPreviousPath, '✔');
        }
    }

    /**
     * Updates the progress with a new character to signal progress.
     *
     * @param data the FuzzingData context
     */
    public void updateUnknownProgress(PlaybookData data) {
        this.notifySummaryObservers(data.getContractPath());
    }

    /**
     * Starts a new testing session, initializing necessary configurations and logging session information.
     * This method sets default values for identifiers and logs session details such as application name,
     * version, build time, and platform.
     */
    public void startSession() {
        MDC.put(ID_ANSI, this.getKeyDefault());
        MDC.put(PLAYBOOK, this.getKeyDefault());
        MDC.put(PLAYBOOK_KEY, this.getKeyDefault());

        ConsoleUtils.emptyLine();
        logger.start(ansi().bold().a("Starting {}-{}, build time {} UTC").reset().toString(),
                ansi().fg(Ansi.Color.GREEN).a(appName),
                ansi().fg(Ansi.Color.GREEN).a(appVersion),
                ansi().fg(Ansi.Color.GREEN).a(appBuildTime));
    }

    /**
     * Initializes the reporting path using the associated test case exporter.
     *
     * @throws IOException if an I/O error occurs during the initialization process
     */
    public void initReportingPath() throws IOException {
        testReportsGenerator.initPath(null);
    }

    /**
     * Initializes the reporting path using the associated test case exporter.
     *
     * @param folder the folder path where the reporting should be initialized
     * @throws IOException if an I/O error occurs during the initialization process
     */
    public void initReportingPath(String folder) throws IOException {
        testReportsGenerator.initPath(folder);
    }

    /**
     * Writes an individual test case using the associated test case exporter.
     *
     * @param testCase the TestCase to be written
     */
    public void writeIndividualTestCase(TestCase testCase) {
        testReportsGenerator.writeTestCase(testCase);
    }

    /**
     * Writes helper files using the associated test case exporter.
     * This method delegates the task of writing helper files to the underlying test case exporter.
     */
    public void writeHelperFiles() {
        testReportsGenerator.writeHelperFiles();
    }

    /**
     * Ends the test session by performing necessary actions such as writing summaries, helper files, and performance reports.
     * Additionally, prints execution details using the associated logger.
     */
    public void endSession() {
        try {
            markPreviousPathAsDone();
            reportingArguments.enableAdditionalLoggingIfSummary();
            testReportsGenerator.writeSummary(testCaseSummaryDetails, executionStatisticsListener);
            testReportsGenerator.writeHelperFiles();
            ConsoleUtils.emptyLine();
            testReportsGenerator.writeErrorsByReason(testCaseSummaryDetails);
            testReportsGenerator.writePerformanceReport(testCaseExecutionDetails);
            testReportsGenerator.printExecutionDetails(executionStatisticsListener);
            writeRecordedErrorsIfPresent();
        } catch (Exception e) {
            logger.error("Error while ending sessions {}", e.getMessage());
        }
    }

    /**
     * Renders a starting header if logging is SUMMARY.
     */
    public void renderStartHeader() {
        if (reportingArguments.isSummaryInConsole()) {
            ConsoleUtils.emptyLine();
            ConsoleUtils.renderHeader("Running tests...");
        }
    }

    private void writeRecordedErrorsIfPresent() {
        globalContext.writeRecordedErrorsIfPresent();
    }

    private void setResultReason(ResultFactory.Result result) {
        TestCase testCase = currentTestCase();
        testCase.setResultReason(result.reason());
    }

    private void setResultReason(String reason) {
        TestCase testCase = currentTestCase();
        testCase.setResultReason(reason);
    }

    /**
     * If {@code --ignore-codes} is supplied and the response code received from the service
     * is in the ignored list, the method will actually report INFO instead of WARN.
     * If {@code skipReportingForIgnoredCodes} is also enabled, the reporting for these ignored codes will be skipped entirely.
     *
     * @param logger  the current logger
     * @param message message to be logged
     * @param params  params needed by the message
     */
    void reportWarn(PrettyLogger logger, String message, Object... params) {
        this.logger.debug("Reporting warn with message: {}", replaceBrackets(message, params));
        TestCase testCase = currentTestCase();
        HttpResponse httpResponse = Optional.ofNullable(testCase.getResponse()).orElse(HttpResponse.empty());
        List<String> detectedKeyWords = testCase.getErrorLeaks();

        if (!ignoreArguments.isIgnoreErrorLeaksCheck() && !detectedKeyWords.isEmpty()) {
            logger.debug("Detected keywords in response body: {}", detectedKeyWords);
            reportError(logger, ResultFactory.createErrorLeaksDetectedInResponse(detectedKeyWords));
        } else if (ignoreArguments.isSkipReportingForWarnings()) {
            this.logger.debug(RECEIVED_RESPONSE_IS_MARKED_AS_IGNORED_SKIPPING);
            this.skipTest(logger, replaceBrackets("Skip reporting as --hide-warnings is enabled"));
            this.recordResult(message, params, SKIP_REPORTING, logger);
        } else if (ignoreArguments.isNotIgnoredResponse(httpResponse)) {
            this.logger.debug("Received response is not marked as ignored... reporting warn!");
            executionStatisticsListener.increaseWarns(testCase.getContractPath());
            logger.warning(message, params);
            this.recordResult(message, params, "warning", logger);
        } else if (ignoreArguments.isSkipReportingForIgnoredArguments()) {
            this.logger.debug(RECEIVED_RESPONSE_IS_MARKED_AS_IGNORED_SKIPPING);
            this.skipTest(logger, replaceBrackets("Some response elements were marked as filtered through --filter-*** arguments."));
            this.recordResult(message, params, SKIP_REPORTING, logger);
        } else {
            testCase.setResultIgnoreDetails("warning");
            this.logger.debug("Received response is marked as ignored... reporting info!");
            this.reportInfo(logger, message, params);
        }
    }

    private void extractErrorLeaks() {
        TestCase testCase = currentTestCase();
        if (testCase.getResponse() == null) {
            return;
        }
        List<String> keywords = WordUtils.getKeywordsMatching(testCase.getResponse().getBody(), globalContext.getErrorLeaksKeywords());
        testCase.setErrorLeaks(keywords);
    }

    private void reportWarnOrInfoBasedOnCheck(PrettyLogger logger, PlaybookData data, ResultFactory.Result result, boolean ignoreCheck, Object... params) {
        if (ignoreCheck) {
            setResultReason(result);
            currentTestCase().setResultIgnoreDetails("warning");
            this.reportInfo(logger, result, params);
        } else {
            this.reportResultWarn(logger, data, result.reason(), result.message(), params);
        }
    }


    /**
     * Reports a warning result for a test using the provided logger, reason, message, and parameters.
     *
     * @param logger  the logger used to log result-related information
     * @param data    the fuzzing data associated with the test
     * @param reason  the reason for the warning result
     * @param message the warning message to be reported
     * @param params  additional parameters to be formatted into the message
     */
    public void reportResultWarn(PrettyLogger logger, PlaybookData data, String reason, String message, Object... params) {
        setResultReason(reason);
        this.reportWarn(logger, message, params);
    }

    private void reportError(PrettyLogger logger, ResultFactory.Result result, Object... params) {
        setResultReason(result);
        this.reportError(logger, result.message(), params);
    }

    /**
     * If {@code --ingore-codes} is supplied and the response code received from the service
     * is in the ignored list, the method will actually report INFO instead of ERROR.
     * If {@code skipReportingForIgnoredCodes} is also enabled, the reporting for these ignored codes will be skipped entirely.
     *
     * @param logger  the current logger
     * @param message message to be logged
     * @param params  params needed by the message
     */
    void reportError(PrettyLogger logger, String message, Object... params) {
        this.logger.debug("Reporting error with message: {}", replaceBrackets(message, params));
        TestCase testCase = currentTestCase();
        HttpResponse httpResponse = Optional.ofNullable(testCase.getResponse()).orElse(HttpResponse.empty());


        if (ignoreArguments.isNotIgnoredResponse(httpResponse) || httpResponse.exceedsExpectedResponseTime(reportingArguments.getMaxResponseTime())
                || isException(httpResponse)) {
            this.logger.debug("Received response is not marked as ignored... reporting error!");
            logAndRecordError(logger, message, params, testCase, httpResponse);
        } else if (hasKeywordLeaks(testCase)) {
            ResultFactory.Result result = ResultFactory.createErrorLeaksDetectedInResponse(testCase.getErrorLeaks());
            setResultReason(result.reason());
            logAndRecordError(logger, result.message(), testCase.getErrorLeaks().toArray(), testCase, httpResponse);
        } else if (ignoreArguments.isSkipReportingForIgnoredArguments()) {
            this.logger.debug(RECEIVED_RESPONSE_IS_MARKED_AS_IGNORED_SKIPPING);
            this.skipTest(logger, "Some response elements were filtered usinbg --filter-* argyments.");
            this.recordResult(message, params, SKIP_REPORTING, logger);
        } else {
            testCase.setResultIgnoreDetails(Level.ERROR.toString());
            this.logger.debug("Received response is marked as ignored... reporting info!");
            this.reportInfo(logger, message, params);
        }
        recordAuthErrors(httpResponse);
    }

    private void logAndRecordError(PrettyLogger logger, String message, Object[] params, TestCase testCase, HttpResponse httpResponse) {
        executionStatisticsListener.increaseErrors(testCase.getContractPath());
        logger.error(message, params);
        this.recordResult(message, params, Level.ERROR.toString().toLowerCase(Locale.ROOT), logger);
        this.renderProgress(httpResponse);
    }

    /**
     * Checks if the response body contains keywords that might suggest security issues or error details leaks.
     * It also checks if the {@code --ignoreErrorLeaksCheck} argument is enabled.
     *
     * @param testCase the current test case
     * @return {@code true} if the response body contains keywords that might suggest security issues or error details leaks; otherwise, {@code false}
     */
    boolean hasKeywordLeaks(TestCase testCase) {
        return !ignoreArguments.isIgnoreErrorLeaksCheck() && !testCase.getErrorLeaks().isEmpty();
    }


    /**
     * When {@code --printProgress} is enabled we output in the console the url that fails.
     *
     * @param httpResponse the HttpResponse object
     */
    private void renderProgress(HttpResponse httpResponse) {
        if (reportingArguments.isPrintProgress()) {
            ConsoleUtils.renderSameRowAndMoveToNextLine("+ " + httpResponse.getPath());
        }
    }

    private boolean isException(HttpResponse httpResponse) {
        return !httpResponse.isValidErrorCode();
    }

    private void recordAuthErrors(HttpResponse httpResponse) {
        if (httpResponse.getResponseCode() == 401 || httpResponse.getResponseCode() == 403) {
            executionStatisticsListener.increaseAuthErrors();
        }
    }

    private void checkForIOErrors(Exception e) {
        if (e.getCause() instanceof IOException) {
            executionStatisticsListener.increaseIoErrors();
        }
    }

    /**
     * Reports an error result for a test using the provided logger, reason, message, and parameters.
     *
     * @param logger  the logger used to log result-related information
     * @param data    the fuzzing data associated with the test
     * @param reason  the reason for the error result
     * @param message the error message to be reported
     * @param params  additional parameters to be formatted into the message
     */
    public void reportResultError(PrettyLogger logger, PlaybookData data, String reason, String message, Object... params) {
        this.reportError(logger, message, params);
        setResultReason(reason);
    }

    private void reportSkipped(PrettyLogger logger, Object... params) {
        executionStatisticsListener.increaseSkipped();
        logger.skip("Skipped due to: {}", params);
        TestCase testCase = currentTestCase();
        testCase.setResultSkipped();
        testCase.setResultDetails(replaceBrackets("Skipped due to: {}", params));
    }

    void reportInfo(PrettyLogger logger, String message, Object... params) {
        TestCase testCase = currentTestCase();
        HttpResponse httpResponse = Optional.ofNullable(testCase.getResponse()).orElse(HttpResponse.empty());
        List<String> detectedKeyWords = testCase.getErrorLeaks();

        if (!ignoreArguments.isIgnoreErrorLeaksCheck() && !detectedKeyWords.isEmpty()) {
            logger.debug("Detected keywords in response body: {}", detectedKeyWords);
            reportError(logger, ResultFactory.createErrorLeaksDetectedInResponse(detectedKeyWords));
        } else if (ignoreArguments.isSkipReportingForSuccess()) {
            this.logger.debug(RECEIVED_RESPONSE_IS_MARKED_AS_IGNORED_SKIPPING);
            this.skipTest(logger, replaceBrackets("Skip reporting as --hide-success is enabled"));
            this.recordResult(message, params, SKIP_REPORTING, logger);
        } else if (ignoreArguments.isIgnoredResponse(httpResponse) && ignoreArguments.isSkipReportingForIgnoredArguments()) {
            this.logger.debug(RECEIVED_RESPONSE_IS_MARKED_AS_IGNORED_SKIPPING);
            this.skipTest(logger, "Some response elements were filtered using --filter-* arguments.");
            this.recordResult(message, params, SKIP_REPORTING, logger);
        } else if (httpResponse.exceedsExpectedResponseTime(reportingArguments.getMaxResponseTime())) {
            this.logger.debug("Received response time exceeds --maxResponseTimeInMs: actual {}, max {}",
                    httpResponse.getResponseTimeInMs(), reportingArguments.getMaxResponseTime());
            this.reportError(logger, ResultFactory.createResponseTimeExceedsMax(httpResponse.getResponseTimeInMs(), reportingArguments.getMaxResponseTime()));
        } else {
            executionStatisticsListener.increaseSuccess(testCase.getContractPath());
            logger.success(message, params);
            this.recordResult(message, params, "success", logger);
        }
    }

    private void reportInfo(PrettyLogger logger, ResultFactory.Result result, Object... params) {
        this.reportInfo(logger, result.message(), params);
    }

    /**
     * Reports an informational result for a test using the provided logger, message, and parameters.
     *
     * @param logger  the logger used to log result-related information
     * @param data    the fuzzing data associated with the test
     * @param message the informational message to be reported
     * @param params  additional parameters to be formatted into the message
     */
    public void reportResultInfo(PrettyLogger logger, PlaybookData data, String message, Object... params) {
        this.reportInfo(logger, message, params);
    }

    /**
     * Reports the result of a test based on the provided parameters, including response code, schema matching, and content type.
     * It also performs checks if response body matches schema.
     *
     * @param logger             the logger used to log result-related information
     * @param data               the fuzzing data associated with the test
     * @param response           the response received from the test
     * @param expectedResultCode the expected response code family
     */
    public void reportResult(PrettyLogger logger, PlaybookData data, HttpResponse response, ResponseCodeFamily expectedResultCode) {
        this.reportResult(logger, data, response, expectedResultCode, true, true);
    }

    /**
     * Reports the result of a test based on the provided parameters, including response code, schema matching, and content type.
     *
     * @param logger                      the logger used to log result-related information
     * @param data                        the fuzzing data associated with the test
     * @param response                    the response received from the test
     * @param expectedResultCode          the expected response code family
     * @param shouldMatchToResponseSchema a flag indicating whether the response should match the expected schema
     */
    public void reportResult(PrettyLogger logger, PlaybookData data, HttpResponse response, ResponseCodeFamily expectedResultCode, boolean shouldMatchToResponseSchema) {
        this.reportResult(logger, data, response, expectedResultCode, shouldMatchToResponseSchema, true);
    }

    /**
     * Reports the result of a test based on the provided parameters, including response code, schema matching, and content type.
     *
     * @param logger                      the logger used to log result-related information
     * @param data                        the fuzzing data associated with the test
     * @param response                    the response received from the test
     * @param expectedResultCode          the expected response code family
     * @param shouldMatchToResponseSchema a flag indicating whether the response should match the expected schema
     * @param shouldMatchContentType      a flag indicating whether the response content type should match the one from the OpenAPI spec
     */
    public void reportResult(PrettyLogger logger, PlaybookData data, HttpResponse response, ResponseCodeFamily expectedResultCode, boolean shouldMatchToResponseSchema, boolean shouldMatchContentType) {
        expectedResultCode = this.getExpectedResponseCodeConfiguredFor(MDC.get(PLAYBOOK_KEY), expectedResultCode);
        boolean matchesResponseSchema = !shouldMatchToResponseSchema || this.matchesResponseSchema(response, data);
        boolean responseCodeExpected = this.isResponseCodeExpected(response, expectedResultCode);
        boolean responseCodeDocumented = this.isResponseCodeDocumented(data, response);
        boolean isResponseContentTypeMatching = !shouldMatchContentType || this.isResponseContentTypeMatching(response, data);

        this.logger.debug("matchesResponseSchema {}, responseCodeExpected {}, responseCodeDocumented {}", matchesResponseSchema, responseCodeExpected, responseCodeDocumented);
        this.storeRequestOnPostOrRemoveOnDelete(data, response);

        ResponseAssertions assertions = ResponseAssertions.builder().matchesResponseSchema(matchesResponseSchema)
                .responseCodeDocumented(responseCodeDocumented).responseCodeExpected(responseCodeExpected).
                responseCodeUnimplemented(ResponseCodeFamily.isUnimplemented(response.getResponseCode()))
                .matchesContentType(isResponseContentTypeMatching).build();

        if (assertions.isNotMatchingContentType() && !ignoreArguments.isIgnoreResponseContentTypeCheck()) {
            this.logger.debug("Response content type not matching contract");
            ResultFactory.Result contentTypeNotMatching = ResultFactory.createNotMatchingContentType(data.getContentTypesByResponseCode(response.responseCodeAsString()), response.getResponseContentType());
            this.reportResultWarn(logger, data, contentTypeNotMatching.reason(), contentTypeNotMatching.message());
        } else if (assertions.isResponseCodeExpectedAndDocumentedAndMatchesResponseSchema()) {
            this.logger.debug("Response code expected and documented and matches response schema");
            this.reportInfo(logger, ResultFactory.createExpectedResponse(response.responseCodeAsString()));
        } else if (assertions.isResponseCodeExpectedAndDocumentedButDoesntMatchResponseSchema()) {
            this.logger.debug("Response code expected and documented but doesn't match response schema");
            this.reportWarnOrInfoBasedOnCheck(logger, data, ResultFactory.createNotMatchingResponseSchema(response.responseCodeAsString()), ignoreArguments.isIgnoreResponseBodyCheck());
        } else if (assertions.isResponseCodeExpectedButNotDocumented()) {
            this.logger.debug("Response code expected but not documented");
            this.reportWarnOrInfoBasedOnCheck(logger, data,
                    ResultFactory.createUndocumentedResponseCode(response.responseCodeAsString(), String.valueOf(expectedResultCode.allowedResponseCodes()), String.valueOf(data.getResponseCodes())),
                    ignoreArguments.isIgnoreResponseCodeUndocumentedCheck());
        } else if (assertions.isResponseCodeDocumentedButNotExpected()) {
            if (isNotFound(response)) {
                this.logger.debug("NOT_FOUND response");
                this.reportError(logger, ResultFactory.createNotFound());
            } else if (assertions.isResponseCodeUnimplemented()) {
                this.logger.debug("Response code unimplemented");
                ResultFactory.Result notImplementedResult = ResultFactory.createNotImplemented();
                this.reportResultWarn(logger, data, notImplementedResult.reason(), notImplementedResult.message());
            } else {
                this.logger.debug("Response code documented but not expected");
                this.reportError(logger, ResultFactory.createUnexpectedResponseCode(response.responseCodeAsString(), expectedResultCode.allowedResponseCodes().toString()));
            }
        } else if (isNotFound(response)) {
            this.logger.debug("NOT_FOUND response");
            this.reportError(logger, ResultFactory.createNotFound());
        } else {
            this.logger.debug("Unexpected behaviour");
            this.reportError(logger, ResultFactory.createUnexpectedBehaviour(response.responseCodeAsString(), expectedResultCode.allowedResponseCodes().toString()));
        }
    }

    private boolean isResponseContentTypeMatching(HttpResponse response, PlaybookData data) {
        boolean noContentTypeDefinedForResponseCode = data.getResponseContentTypes().get(response.responseCodeAsString()) == null;
        boolean responseDoesNotHaveContentType = response.getResponseContentType() == null;
        boolean responseContentTypeDefined = data.getContentTypesByResponseCode(response.responseCodeAsString())
                .stream()
                .anyMatch(contentType -> areContentTypesEquivalent(response.getResponseContentType(), contentType));
        boolean unknownContentType = response.isUnknownContentType();

        return (noContentTypeDefinedForResponseCode && responseDoesNotHaveContentType) || responseContentTypeDefined || unknownContentType;
    }

    boolean areContentTypesEquivalent(String firstContentType, String secondContentType) {
        try {
            MediaType firstMediaType = MediaType.parse(Optional.ofNullable(firstContentType).orElse(HttpResponse.unknownContentType())).withoutParameters();
            MediaType secondMediaType = MediaType.parse(Optional.ofNullable(secondContentType).orElse(HttpResponse.unknownContentType())).withoutParameters();

            return firstMediaType.is(secondMediaType) || secondMediaType.is(firstMediaType) || (firstMediaType.type().equalsIgnoreCase(secondMediaType.type()) &&
                    (firstMediaType.subtype().endsWith(secondMediaType.subtype()) || secondMediaType.subtype().endsWith(firstMediaType.subtype())));
        } catch (IllegalArgumentException e) {
            logger.debug("Error parsing content type: {}", e.getMessage());
            return false;
        }
    }

    private void storeRequestOnPostOrRemoveOnDelete(PlaybookData data, HttpResponse response) {
        if (data.getMethod() == HttpMethod.POST && ResponseCodeFamily.is2xxCode(response.getResponseCode())) {
            logger.star("POST method for path {} returned successfully {}. Storing result for DELETE endpoints...", data.getPath(), response.responseCodeAsString());
            Deque<String> existingPosts = globalContext.getPostSuccessfulResponses().getOrDefault(data.getPath(), new ArrayDeque<>());
            existingPosts.add(response.getBody());
            globalContext.getPostSuccessfulResponses().put(data.getPath(), existingPosts);
        } else if (data.getMethod() == HttpMethod.DELETE && ResponseCodeFamily.is2xxCode(response.getResponseCode())) {
            logger.star("Successful DELETE. Removing top POST request from the store...");
            globalContext.getPostSuccessfulResponses().getOrDefault(data.getPath().substring(0, data.getPath().lastIndexOf("/")), new ArrayDeque<>()).poll();
        }
    }

    private boolean isNotFound(HttpResponse response) {
        return response.getResponseCode() == 404;
    }

    private boolean isResponseCodeDocumented(PlaybookData data, HttpResponse response) {
        Set<String> responseCodes = Optional.ofNullable(data.getResponseCodes()).orElse(Collections.emptySet());
        return responseCodes.contains(response.responseCodeAsString()) ||
                isNotTypicalDocumentedResponseCode(response) ||
                responseMatchesDocumentedRange(response.responseCodeAsResponseRange(), responseCodes);
    }

    private boolean responseMatchesDocumentedRange(String receivedResponseCode, Set<String> documentedResponseCodes) {
        return documentedResponseCodes.stream().anyMatch(code -> code.equalsIgnoreCase(receivedResponseCode));
    }

    /**
     * Skips the current test, adds the skip reason to the expected results, and reports the test as skipped using the provided logger.
     *
     * @param logger     the logger used to log skip-related information
     * @param skipReason the reason for skipping the test
     */
    public void skipTest(PrettyLogger logger, String skipReason) {
        this.addExpectedResult(logger, skipReason);
        this.reportSkipped(logger, skipReason);
    }

    /**
     * Checks if a fuzzed field is not a discriminator based on the configured discriminators in the global context.
     *
     * @param fuzzedField the fuzzed field to check
     * @return true if the fuzzed field is not a discriminator, false otherwise
     */
    public boolean isFieldNotADiscriminator(String fuzzedField) {
        return !globalContext.isDiscriminator(fuzzedField);
    }

    /**
     * Returns the expected HTTP response code from the --playbooks-config file
     *
     * @param playbook     the name of the playbook
     * @param defaultValue default value when property is not found
     * @return the value of the property if found or null otherwise
     */
    public ResponseCodeFamily getExpectedResponseCodeConfiguredFor(String playbook, ResponseCodeFamily defaultValue) {
        String keyToLookup = playbook + "." + "expectedResponseCode";
        String valueFound = globalContext.getExpectedResponseCodeConfigured(keyToLookup);
        logger.debug("Configuration key {}, value {}", keyToLookup, valueFound);

        if (valueFound == null) {
            return defaultValue;
        }
        List<String> responseCodes = Arrays.stream(valueFound.split(",", -1))
                .map(String::trim)
                .filter(item -> item.length() == 3)
                .toList();
        return new ResponseCodeFamilyDynamic(responseCodes);
    }

    private void recordResult(String message, Object[] params, String result, PrettyLogger logger) {
        TestCase testCase = currentTestCase();
        testCase.setResult(result);
        testCase.setResultDetails(replaceBrackets(message, params));
        logger.star("{}, Path {}, HttpMethod {}, Result {}", testCase.getTestId(), testCase.getPath(), Optional.ofNullable(testCase.getRequest()).orElse(HttpRequest.empty()).getHttpMethod(), result);
        storeSuccessfulDelete(testCase);
    }

    void storeSuccessfulDelete(TestCase testCase) {
        if (ResponseCodeFamily.is2xxCode(testCase.getResponse().getResponseCode()) && HttpMethod.DELETE.name().equalsIgnoreCase(testCase.getRequest().getHttpMethod())) {
            globalContext.getSuccessfulDeletes().add(testCase.getRequest().getUrl());
            logger.note("Storing successful DELETE: {}", testCase.getRequest().getUrl());
        }
    }

    /**
     * The response code is expected if the response code received from the server matches the dochia test case expectations.
     * There is also a particular case when we fuzz GET requests, and we reach unimplemented endpoints. This is why we also test for 501
     *
     * @param response           response received from the service
     * @param expectedResultCode what is dochia expecting in this scenario
     * @return {@code true} if the response matches dochia expectations and {@code false} otherwise
     */
    private boolean isResponseCodeExpected(HttpResponse response, ResponseCodeFamily expectedResultCode) {
        return expectedResultCode.matchesAllowedResponseCodes(response.responseCodeAsString());
    }

    private boolean matchesResponseSchema(HttpResponse response, PlaybookData data) {
        try {
            List<String> responses = this.getExpectedResponsesByResponseCode(response, data);

            return isNullResponse(response)
                    || isResponseEmpty(response, responses)
                    || isResponseContentTypeNotMatchable(response)
                    || isNotTypicalDocumentedResponseCode(response)
                    || isEmptyArray(response.getJsonBody())
                    || isActualResponseMatchingDocumentedResponses(response, responses);
        } catch (Exception e) {
            logger.debug("Something happened while matching response schema!", e);
            //if something happens during json parsing we consider it doesn't match schema
            return false;
        }
    }

    private boolean isNullResponse(HttpResponse response) {
        return response.getJsonBody() == null || response.getBody() == null;
    }

    private boolean isResponseContentTypeNotMatchable(HttpResponse response) {
        return CONTENT_TYPE_DONT_MATCH_SCHEMA
                .stream()
                .anyMatch(dontMatchContentType -> areContentTypesEquivalent(dontMatchContentType, response.getResponseContentType()));
    }

    private boolean isEmptyArray(JsonElement jsonElement) {
        return jsonElement.isJsonArray() && isEmptyBody(jsonElement.toString());
    }

    private List<String> getExpectedResponsesByResponseCode(HttpResponse response, PlaybookData data) {
        Map<String, List<String>> responsesMap = Optional.ofNullable(data.getResponses()).orElse(Collections.emptyMap());
        List<String> responses = responsesMap.get(response.responseCodeAsString());

        if (CollectionUtils.isEmpty(responses)) {
            return responsesMap.getOrDefault(response.responseCodeAsResponseRange(),
                    responsesMap.get(response.responseCodeAsResponseRange().toLowerCase(Locale.ROOT)));
        }

        return responses;
    }

    private boolean isActualResponseMatchingDocumentedResponses(HttpResponse response, List<String> responses) {
        return responses != null && responses.stream().anyMatch(responseSchema -> matchesElement(responseSchema, response.getJsonBody()))
                && (isFuzzedFieldPresentInResponse(response) || !isErrorResponse(response));
    }

    private boolean isErrorResponse(HttpResponse response) {
        return ResponseCodeFamilyPredefined.FOURXX.matchesAllowedResponseCodes(response.responseCodeAsString());
    }

    private boolean isFuzzedFieldPresentInResponse(HttpResponse response) {
        return response.getTestedField() == null ||
                response.getBody()
                        .replaceAll("[-_\\s]+", "")
                        .toLowerCase(Locale.ROOT)
                        .contains(response.getTestedField()
                                .replaceAll("[-_#\\s]+", "")
                                .toLowerCase(Locale.ROOT));
    }

    private boolean isNotTypicalDocumentedResponseCode(HttpResponse response) {
        return NOT_NECESSARILY_DOCUMENTED.contains(response.responseCodeAsString());
    }

    private boolean isResponseEmpty(HttpResponse response, List<String> responses) {
        return (responses == null || responses.isEmpty()) && isEmptyBody(response.getBody());
    }

    private boolean isEmptyBody(String body) {
        boolean isEmptyString = body.trim().isEmpty();
        boolean isEmptyArray = body.trim().equalsIgnoreCase("[]");
        boolean isEmptyJson = body.trim().equalsIgnoreCase("{}");

        return isEmptyString || isEmptyArray || isEmptyJson;
    }

    private boolean matchesElement(String responseSchema, JsonElement element) {
        if (element.isJsonArray()) {
            return matchesArrayElement(responseSchema, element);
        }

        return matchesSingleElement(responseSchema, element, "ROOT");
    }

    private boolean matchesArrayElement(String responseSchema, JsonElement element) {
        JsonArray jsonArray = ((JsonArray) element);

        if (jsonArray.isEmpty() && JsonParser.parseString(responseSchema).isJsonArray()) {
            return true;
        } else if (jsonArray.isEmpty()) {
            return false;
        }

        JsonElement firstElement = jsonArray.get(0);
        return matchesSingleElement(responseSchema, firstElement, "ROOT");
    }

    private boolean matchesSingleElement(String responseSchema, JsonElement element, String name) {
        if (element.isJsonObject() && globalContext.getAdditionalProperties().containsKey(name)) {
            return true;
        }
        if (doesNotHaveAResponseSchema(responseSchema)) {
            return true;
        }
        if (!element.isJsonObject()) {
            return responseSchema.toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT));
        }

        boolean matches = true;
        for (Map.Entry<String, JsonElement> inner : element.getAsJsonObject().entrySet()) {
            matches = matches && matchesSingleElement(responseSchema, inner.getValue(), inner.getKey());
        }

        return matches;
    }

    private static boolean doesNotHaveAResponseSchema(String responseSchema) {
        return responseSchema == null || responseSchema.isEmpty();
    }

    private TestCase currentTestCase() {
        return testCaseMap.get(MDC.get(ID));
    }

    public void recordError(String error) {
        globalContext.recordError(error);
    }

    @Builder
    static class ResponseAssertions {
        private final boolean matchesResponseSchema;
        private final boolean responseCodeExpected;
        private final boolean responseCodeDocumented;
        private final boolean responseCodeUnimplemented;
        private final boolean matchesContentType;

        private boolean isNotMatchingContentType() {
            return !matchesContentType;
        }

        private boolean isResponseCodeExpectedAndDocumentedAndMatchesResponseSchema() {
            return matchesResponseSchema && responseCodeDocumented && responseCodeExpected;
        }

        private boolean isResponseCodeExpectedAndDocumentedButDoesntMatchResponseSchema() {
            return !matchesResponseSchema && responseCodeDocumented && responseCodeExpected;
        }

        private boolean isResponseCodeDocumentedButNotExpected() {
            return responseCodeDocumented && !responseCodeExpected;
        }

        private boolean isResponseCodeExpectedButNotDocumented() {
            return responseCodeExpected && !responseCodeDocumented;
        }

        private boolean isResponseCodeUnimplemented() {
            return responseCodeUnimplemented;
        }
    }
}
