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
    @CommandLine.Option(names = {"-l", "--log"}, paramLabel = "<package:level>",
            description = "Sets custom log level of a given package(s). This can be a comma separated list of @|bold,underline package:level|@ pairs or a global log level. This is intended more for debugging purposes", split = ",")
    private List<String> logData;

    @CommandLine.Option(names = {"-g", "--skip-log"}, paramLabel = "<level>",
            description = "A list of log levels to skip. For example, only @|bold,underline note|@ and @|bold,underline info|@ levels can be skipped, but the rest remain. By default it skips @|bold,underline note, skip|@ levels which are used to enable more detailed traceability" +
                    " If supplied, this will override the --only-log argument", split = ",")
    private List<String> skipLogs;

    @CommandLine.Option(names = {"-O", "--only-log"}, paramLabel = "<level>",
            description = "A list of log levels to include. For example, only @|bold,underline fav|@ and @|bold,underline warning|@ levels can be included, while the rest are left out", split = ",")
    private List<String> onlyLog;

    @CommandLine.Option(names = {"-D", "--debug"},
            description = "Set log level to ALL. Useful for diagnosis when raising bugs")
    private boolean debug;

    @CommandLine.Option(names = {"--print-execution-statistics"},
            description = "Print a summary of execution times for each endpoint and HTTP method. By default this will print a summary for each endpoint: max, min and average. Detailed reports can be enabled using @|bold --printDetailedExecutionStatistics|@")
    private boolean printExecutionStatistics;

    @CommandLine.Option(names = {"--print-detailed-execution-statistics"},
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

    @CommandLine.Option(names = {"--max-response-time-in-ms"}, paramLabel = "<max>",
            description = "Sets a response time limit in milliseconds. If responses take longer than the provided value, they will get marked as @|bold error|@ with reason @|underline Response time exceeds max|@." +
                    " The response time limit check is triggered only if the test case is considered successful i.e. response matches Playbook expectations")
    private int maxResponseTime;

    @CommandLine.Option(names = {"--verbosity"}, paramLabel = "<verbosity>",
            description = "Sets the verbosity of the console logging. If set to @|bold summary|@ the output is a simple progress per path. Default: @|bold,underline ${DEFAULT-VALUE}|@")
    private Verbosity verbosity = Verbosity.SUMMARY;

    @CommandLine.Option(names = {"--mask-headers"}, paramLabel = "<header>",
            description = "A list of headers to mask when logging into console or in report files. Headers will be replaced with @|underline $$headerName|@ so that test cases can be replayed with environment variables", split = ",")
    private Set<String> maskHeaders;

    @CommandLine.Option(names = {"--print-progress"},
            description = "If set to true, it will print any URLs matching the given match arguments.  Default: @|bold,underline ${DEFAULT-VALUE}|@")
    boolean printProgress;

    private List<String> getLogData() {
        return Optional.ofNullable(logData).orElse(Collections.emptyList());
    }

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
        if (verbosity == Verbosity.SUMMARY) {
            prepareSummaryLogging();
        } else {
            prepareDetailedLogging();
        }
    }

    private void prepareSummaryLogging() {
        PrettyLogger.enableLevels(PrettyLevel.CONFIG, PrettyLevel.FATAL);
    }

    private void prepareDetailedLogging() {
        for (String logLine : this.getLogData()) {
            String[] log = logLine.strip().trim().split(":", -1);
            String level;
            String pkg = "dev.dochia.cli";
            if (log.length == 1) {
                level = log[0];
            } else {
                level = log[1];
                pkg = log[0];
            }
            CommonUtils.setLogLevel(pkg, level);
        }
        if (debug) {
            CommonUtils.setDochiaLogLevel("ALL");
            logger.fav("Setting dochia log level to ALL!");
        }
        PrettyLogger.enableLevels(getAsPrettyLevelList(this.onlyLog).toArray(new PrettyLevel[0]));
        PrettyLogger.disableLevels(getAsPrettyLevelList(this.skipLogs).toArray(new PrettyLevel[0]));

        /*if no input is supplied, by default note and skip are not logged*/
        if (this.skipLogs == null && this.onlyLog == null) {
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
        return verbosity == Verbosity.SUMMARY;
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
