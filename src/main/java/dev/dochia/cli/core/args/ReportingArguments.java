package dev.dochia.cli.core.args;

import dev.dochia.cli.core.util.CommonUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.github.ludovicianul.prettylogger.config.level.PrettyLevel;
import jakarta.inject.Singleton;
import lombok.Getter;
import picocli.CommandLine;

import java.util.*;

/**
 * Holds all arguments relate to how to handle output.
 */
@Singleton
@Getter
public class ReportingArguments {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(ReportingArguments.class);

    @CommandLine.Option(names = {"--execution-stats"},
            description = "Print a summary of execution times for each endpoint and HTTP method. By default this will print a summary for each endpoint: max, min and average. Detailed reports can be enabled using @|bold --detailed-execution-stats|@")
    private boolean printExecutionStatistics;

    @CommandLine.Option(names = {"--detailed-execution-stats"},
            description = "Print detailed execution statistics with execution times for each request")
    private boolean printDetailedExecutionStatistics;

    @CommandLine.Option(names = {"--timestamp-reports"},
            description = "Output the report inside the @|bold dochia-report|@ folder in a sub-folder with the current timestamp")
    private boolean timestampReports;

    @CommandLine.Option(names = {"--output-format"}, paramLabel = "<format>",
            description = "A list report formats. Default: @|bold,underline ${DEFAULT-VALUE}|@. For example, the @|bold,underline HTML_ONLY|@ report format does not contain any Javascript. This is useful for large number of tests, as the page will render faster and also in CI environments due to Javascript content security policies", split = ",")
    private List<ReportFormat> reportFormat = List.of(ReportFormat.HTML_JS);

    @CommandLine.Option(names = {"-o", "--output"}, paramLabel = "<file>",
            description = "The output folder of the reports. Default: @|bold,underline dochia-report|@ in the current directory")
    private String outputReportFolder = "dochia-report";

    @CommandLine.Option(names = {"-j", "--json"},
            description = "Specifies if output from --dry-run is in json format.")
    private boolean jsonOutput;

    @CommandLine.Option(names = {"--check-update"}, negatable = true, defaultValue = "true", fallbackValue = "true",
            description = "Indicates if there is an update available and prints the release notes along with the download link.")
    private boolean checkUpdate = true;

    @CommandLine.Option(names = {"--color"}, negatable = true, defaultValue = "true", fallbackValue = "true",
            description = "Indicates if coloured output is enabled in the console.")
    private boolean color = true;

    @CommandLine.Option(names = {"--max-response-time"}, paramLabel = "<ms>",
            description = "Sets a response time limit in milliseconds. If responses take longer than the provided value, they will get marked as @|bold error|@ with reason @|underline Response time exceeds max|@." +
                    " The response time limit check is triggered only if the test case is considered successful i.e. response matches Playbook expectations")
    private int maxResponseTime;

    @CommandLine.Option(names = {"-v"}, paramLabel = "<verbosity>",
            description = "Sets the verbosity of the console logging. Specify multiple -v options to increase verbosity. For example, -v, -vv, -vvv")
    private boolean[] verbosity;

    @CommandLine.Option(names = {"--mask-headers"}, paramLabel = "<header>",
            description = "A list of headers to mask when logging into console or in report files. Headers will be replaced with @|underline $$headerName|@ so that test cases can be replayed with environment variables", split = ",")
    private Set<String> maskHeaders;

    @CommandLine.Option(names = {"--print-progress"},
            description = "If set to true, it will print any URLs matching the given match arguments.  Default: @|bold,underline ${DEFAULT-VALUE}|@")
    boolean printProgress;

    /**
     * Return the give log list as PrettyLogger levels.
     *
     * @param logsAsString the list of logs
     * @return a list of PrettyLogger levels
     */
    public static List<PrettyLevel> getAsPrettyLevelList(List<String> logsAsString) {
        return Optional.ofNullable(logsAsString).orElse(Collections.emptyList())
                .stream()
                .filter(entry -> Arrays.stream(PrettyLevel.values())
                        .map(PrettyLevel::name)
                        .anyMatch(level -> level.equalsIgnoreCase(entry)))
                .map(String::toUpperCase)
                .map(PrettyLevel::valueOf)
                .toList();
    }


    /**
     * Processes log data based on --verbosity.
     */
    public void processLogData() {
        if (isVerbosityOne()) {
            PrettyLogger.enableLevels(PrettyLevel.CONFIG, PrettyLevel.FATAL, PrettyLevel.TIMER);
        } else if (isNoVerbosity()) {
            PrettyLogger.enableLevels(PrettyLevel.FATAL, PrettyLevel.TIMER);
        } else {
            prepareDetailedLogging();
        }
    }

    private boolean isNoVerbosity() {
        return verbosity == null || verbosity.length == 0;
    }

    private boolean isVerbosityOne() {
        return verbosity != null && verbosity.length == 1;
    }

    private boolean isVerbosityTwo() {
        return verbosity != null && verbosity.length == 2;
    }

    private boolean isVerbosityThree() {
        return verbosity != null && verbosity.length >= 3;
    }

    private void prepareDetailedLogging() {
        if (isVerbosityThree()) {
            CommonUtils.setDochiaLogLevel("ALL");
            logger.fav("Setting dochia log level to ALL!");
        }

        if (isVerbosityTwo()) {
            PrettyLogger.disableLevels(getAsPrettyLevelList(List.of("note", "skip")).toArray(new PrettyLevel[0]));
        }
    }

    /**
     * Enables additional logging typically needed to log statistical data after testing is performed.
     */
    public void enableAdditionalLoggingIfSummary() {
        if (this.isSummaryInConsole()) {
            PrettyLogger.enableLevels(PrettyLevel.STAR, PrettyLevel.COMPLETE, PrettyLevel.NONE, PrettyLevel.INFO, PrettyLevel.TIMER, PrettyLevel.FATAL);
        }
    }


    /**
     * Returns the maskedHeaders list or an empty collection if maskedHeaders is null;
     *
     * @return the masked headers list
     */
    public Set<String> getMaskedHeaders() {
        return Optional.ofNullable(maskHeaders).orElse(Collections.emptySet());
    }

    /**
     * Check --verbosity.
     *
     * @return true if --verbosity=SUMMARY, false otherwise
     */
    public boolean isSummaryInConsole() {
        return !isVerbosityTwo() && !isVerbosityThree();
    }

    /**
     * Enumerates different formats for generating reports.
     */
    public enum ReportFormat {

        /**
         * Generates a report in HTML format without JavaScript.
         */
        HTML_ONLY,
        /**
         * Generates a report in HTML format with JavaScript support.
         */
        HTML_JS
    }

    /**
     * Enumerates different levels of verbosity for displaying information.
     */
    public enum Verbosity {
        /**
         * Provides a summary level of information.
         */
        SUMMARY,
        /**
         * Provides a detailed level of information.
         */
        DETAILED
    }

}
