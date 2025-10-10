package dev.dochia.cli.core.command;

import dev.dochia.cli.core.args.*;
import dev.dochia.cli.core.command.model.ConfigOptions;
import dev.dochia.cli.core.command.model.HelpFullOption;
import dev.dochia.cli.core.context.GlobalContext;
import dev.dochia.cli.core.exception.DochiaException;
import dev.dochia.cli.core.factory.PlaybookDataFactory;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.model.DochiaConfiguration;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.report.ExecutionStatisticsListener;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.util.*;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.quarkus.arc.Unremovable;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import jakarta.inject.Inject;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.fusesource.jansi.Ansi;
import picocli.CommandLine;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Main application command.
 */
@CommandLine.Command(
        name = "test",
        mixinStandardHelpOptions = true,
        usageHelpAutoWidth = true,
        versionProvider = VersionProvider.class,
        commandListHeading = "%n@|bold,underline Commands:|@%n",
        defaultValueProvider = CommandLine.PropertiesDefaultProvider.class,
        description = "Automatically generate and run negative and boundary tests from your OpenAPI spec using 100+ fuzzing playbooks.",
        exitCodeOnInvalidInput = 191,
        exitCodeOnExecutionException = 192,
        abbreviateSynopsis = true,
        synopsisHeading = "@|bold,underline Usage:|@%n",
        customSynopsis = {
                "@|bold dochia|@ @|fg(yellow) test -c|@ <contract> @|fg(yellow) -s|@ <server> [ADDITIONAL OPTIONS]",
                ""
        },
        exitCodeListHeading = "%n@|bold,underline Exit Codes:|@%n",
        exitCodeList = {
                "@|bold  0|@:Successful program execution",
                "@|bold 191|@:Usage error: user input for the command was incorrect",
                "@|bold 192|@:Internal execution error: an exception occurred when executing command",
                "@|bold ERR|@:Where ERR is the number of errors reported by dochia"
        },
        footerHeading = "%n@|bold,underline Examples:|@%n",
        footer = {
                "  Run in blackbox mode and only report 500 http error codes:",
                "    dochia test -c openapi.yml -s http://localhost:8080 -b",
                "",
                "  Run with authentication headers from an environment variable called TOKEN:",
                "    dochia test -c openapi.yml -s http://localhost:8080 -H API-Token=$$TOKEN"
        })
@Unremovable
public class TestCommand implements Runnable, CommandLine.IExitCodeGenerator {

    private final PrettyLogger logger;
    private static final String SEPARATOR = "-".repeat(ConsoleUtils.getConsoleColumns(22));
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    PlaybookDataFactory playbookDataFactory;
    @Inject
    TestCaseListener testCaseListener;

    @CommandLine.Mixin
    ConfigOptions configOptions;

    @CommandLine.Mixin
    HelpFullOption helpFullOption;

    @Inject
    @CommandLine.ArgGroup(heading = "%n@|bold,underline API Options:|@%n", exclusive = false)
    ApiArguments apiArguments;

    @Inject
    @CommandLine.ArgGroup(
            heading = "%n@|bold,underline Authentication Options:|@%n",
            exclusive = false)
    AuthArguments authArgs;

    @Inject
    @CommandLine.ArgGroup(heading = "%n@|bold,underline Test Control Options:|@%n", exclusive = false)
    CheckArguments checkArgs;

    @Inject
    @CommandLine.ArgGroup(heading = "%n@|bold,underline ConfigurationFiles Options:|@%n", exclusive = false)
    FilesArguments filesArguments;

    @Inject
    @CommandLine.ArgGroup(heading = "%n@|bold,underline Filter and Selection Options:|@%n", exclusive = false)
    FilterArguments filterArguments;

    @Inject
    @CommandLine.ArgGroup(heading = "%n@|bold,underline Response Handling Options:|@%n", exclusive = false)
    IgnoreArguments ignoreArguments;

    @Inject
    @CommandLine.ArgGroup(heading = "%n@|bold,underline Processing and GenerationOptions:|@%n", exclusive = false)
    ProcessingArguments processingArguments;

    @Inject
    @CommandLine.ArgGroup(heading = "%n@|bold,underline Reporting and Output Options:|@%n", exclusive = false)
    ReportingArguments reportingArguments;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Inject
    ExecutionStatisticsListener executionStatisticsListener;

    @Inject
    GlobalContext globalContext;

    @Inject
    VersionChecker versionChecker;

    @Getter
    @ConfigProperty(name = "quarkus.application.version", defaultValue = "1.0.0")
    String appVersion;

    private int exitCodeDueToErrors;

    /**
     * Creates a new instance of TestCommand.
     */
    public TestCommand() {
        logger = PrettyLoggerFactory.getLogger(TestCommand.class);
    }

    @Override
    public void run() {
        Future<VersionChecker.CheckResult> newVersion = this.checkForNewVersion();
        try {
            checkIfNotArgs();
            testCaseListener.startSession();
            this.doLogic();
        } catch (DochiaException | IOException | IllegalArgumentException e) {
            logger.fatal("Something went wrong while running dochia: {}", e.toString());
            logger.debug("Stacktrace: {}", e);
            exitCodeDueToErrors = 192;
        } finally {
            testCaseListener.endSession();
            this.printSuggestions();
            try {
                this.printVersion(newVersion);
            } catch (ExecutionException | InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Stacktrace: {}", e);
            }
        }
    }

    private void checkIfNotArgs() {
        if (apiArguments.getContract() == null && apiArguments.getServer() == null) {
            System.out.println(ConsoleUtils.SHORT_HELP);
            System.exit(0);
        }
    }

    void printVersion(Future<VersionChecker.CheckResult> newVersion)
            throws ExecutionException, InterruptedException {
        VersionChecker.CheckResult checkResult = newVersion.get();
        logger.debug("Current version {}. Latest version {}", appVersion, checkResult.getVersion());
        if (checkResult.isNewVersion()) {
            String message =
                    ansi()
                            .bold()
                            .fgBrightBlue()
                            .a("A new version is available: {}. Download url: {}")
                            .reset()
                            .toString();
            String currentVersionFormatted =
                    ansi()
                            .bold()
                            .fgBrightBlue()
                            .a(checkResult.getVersion())
                            .reset()
                            .bold()
                            .fgBrightBlue()
                            .toString();
            String downloadUrlFormatted =
                    ansi().bold().fgGreen().a(checkResult.getDownloadUrl()).reset().toString();
            logger.star(message, currentVersionFormatted, downloadUrlFormatted);
        }
    }

    private void doLogic() throws IOException {
        this.prepareRun();
        OpenAPI openAPI = this.createOpenAPI();
        this.checkOpenAPI(openAPI);
        apiArguments.validateValidServer(spec, openAPI);
        // reporting path is initialized only if OpenAPI spec is successfully parsed
        testCaseListener.initReportingPath();
        this.printConfiguration(openAPI);
        this.initGlobalData(openAPI);
        this.startFuzzing(openAPI);
    }

    private void checkOpenAPI(OpenAPI openAPI) {
        if (openAPI == null || openAPI.getPaths() == null || openAPI.getPaths().isEmpty()) {
            throw new IllegalArgumentException("Provided OpenAPI specs are invalid!");
        }
    }

    Future<VersionChecker.CheckResult> checkForNewVersion() {
        Callable<VersionChecker.CheckResult> versionCallable =
                () -> VersionChecker.CheckResult.builder().build();
        if (reportingArguments.isCheckUpdate()) {
            versionCallable = () -> versionChecker.checkForNewVersion(this.appVersion);
        }
        return executor.submit(versionCallable);
    }

    private void printSuggestions() {
        if (executionStatisticsListener.areManyAuthErrors()) {
            String message =
                    ansi()
                            .bold()
                            .fgBrightYellow()
                            .a(
                                    "There were {} tests failing with authorisation errors. Either supply authentication details or check if the supplied credentials are correct!")
                            .reset()
                            .toString();
            logger.star(message, executionStatisticsListener.getAuthErrors());
        }
        if (executionStatisticsListener.areManyIoErrors()) {
            String message =
                    ansi()
                            .bold()
                            .fgBrightYellow()
                            .a(
                                    "There were {} tests failing with i/o errors. Make sure that you have access to the service or that the --server url is correct!")
                            .reset()
                            .toString();
            logger.star(message, executionStatisticsListener.getIoErrors());
        }
    }

    private void initGlobalData(OpenAPI openAPI) {
        DochiaConfiguration dochiaConfiguration =
                new DochiaConfiguration(
                        appVersion,
                        apiArguments.getContract(),
                        apiArguments.getServer(),
                        filterArguments.getHttpMethods(),
                        filterArguments.getFirstPhasePlaybooksForPath().size()
                                + filterArguments.getSecondPhasePlaybooks().size(),
                        filterArguments.getTotalPlaybooks(),
                        filterArguments.getPathsToRun(openAPI).size(),
                        openAPI.getPaths().size());

        globalContext.init(
                openAPI,
                processingArguments.getContentType(),
                filesArguments.getPlaybookConfigProperties(),
                dochiaConfiguration,
                filesArguments.getErrorLeaksKeywordsList());

        logger.debug("Playbooks custom configuration: {}", globalContext.getPlaybooksConfiguration());
        logger.debug("Schemas: {}", globalContext.getSchemaMap().keySet());
    }

    void startFuzzing(OpenAPI openAPI) {
        testCaseListener.renderStartHeader();

        List<String> suppliedPaths = filterArguments.getPathsToRun(openAPI);

        for (Map.Entry<String, PathItem> entry :
                this.sortPathsAlphabetically(openAPI, filesArguments.getPathsOrder())) {
            if (suppliedPaths.contains(entry.getKey())) {
                this.fuzzPath(entry, openAPI);
            } else {
                logger.skip("Skipping path {}", entry.getKey());
            }
        }
    }

    private Set<Map.Entry<String, PathItem>> sortPathsAlphabetically(
            OpenAPI openAPI, List<String> pathsOrder) {
        Comparator<Map.Entry<String, PathItem>> customComparator =
                CommonUtils.createCustomComparatorBasedOnPathsOrder(pathsOrder);

        Set<Map.Entry<String, PathItem>> pathsOrderedAlphabetically =
                openAPI.getPaths().entrySet().stream()
                        .sorted(customComparator)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        logger.debug(
                "Paths ordered alphabetically: {}",
                pathsOrderedAlphabetically.stream().map(Map.Entry::getKey).toList());

        return pathsOrderedAlphabetically;
    }

    OpenAPI createOpenAPI() throws IOException {
        String finishMessage =
                ansi().fgGreen().a("Finished parsing the contract in {} ms").reset().toString();
        long t0 = System.currentTimeMillis();
        OpenAPI openAPI = OpenApiUtils.readOpenApi(apiArguments.getContract());
        logger.debug(finishMessage, (System.currentTimeMillis() - t0));
        return openAPI;
    }

    void prepareRun() throws IOException {
        // this is a hack to set terminal width here in order to avoid importing a full-blown library
        // like jline
        // just for getting the terminal width
        ConsoleUtils.initTerminalWidth(spec);
        reportingArguments.processLogData();
        apiArguments.validateRequired(spec);
        filesArguments.loadConfig();
    }

    private void printConfiguration(OpenAPI openAPI) {
        logger.config(
                ansi().bold().a("OpenAPI specs: {}").reset().toString(),
                ansi().fg(Ansi.Color.BLUE).a(apiArguments.getContract()).reset());
        logger.config(
                ansi().bold().a("API base url: {}").reset().toString(),
                ansi().fg(Ansi.Color.BLUE).a(apiArguments.getServer()).reset());
        logger.config(
                ansi().bold().a("Reporting path: {}").reset().toString(),
                ansi().fg(Ansi.Color.BLUE).a(reportingArguments.getOutputReportFolder()).reset());
        logger.config(
                ansi().bold().a("{} configured playbooks out of {} total playbooks").bold().reset().toString(),
                ansi()
                        .fg(Ansi.Color.BLUE)
                        .a(filterArguments.getFirstPhasePlaybooksForPath().size())
                        .reset()
                        .bold(),
                ansi()
                        .fg(Ansi.Color.BLUE)
                        .a(filterArguments.getAllRegisteredPlaybooks().size())
                        .reset()
                        .bold());
        logger.config(
                ansi()
                        .bold()
                        .a("{} configured paths out of {} total OpenAPI paths")
                        .bold()
                        .reset()
                        .toString(),
                ansi()
                        .fg(Ansi.Color.BLUE)
                        .a(filterArguments.getPathsToRun(openAPI).size())
                        .bold()
                        .reset()
                        .bold(),
                ansi().fg(Ansi.Color.BLUE).a(openAPI.getPaths().size()).reset().bold());
        logger.config(
                ansi().bold().a("HTTP methods in scope: {}").reset().toString(),
                ansi().fg(Ansi.Color.BLUE).a(filterArguments.getHttpMethods()).reset());
        logger.config(
                ansi()
                        .bold()
                        .bold()
                        .a(
                                "Example flags: use-request-body-examples {}, use-schema-examples {}, use-property-examples {}, use-response-body-examples {}, use-defaults {}")
                        .reset()
                        .toString(),
                ansi().fg(Ansi.Color.BLUE).a(processingArguments.isUseRequestBodyExamples()).reset().bold(),
                ansi().fg(Ansi.Color.BLUE).a(processingArguments.isUseSchemaExamples()).reset().bold(),
                ansi().fg(Ansi.Color.BLUE).a(processingArguments.isUsePropertyExamples()).reset().bold(),
                ansi()
                        .fg(Ansi.Color.BLUE)
                        .a(processingArguments.isUseResponseBodyExamples())
                        .reset()
                        .bold(),
                ansi().fg(Ansi.Color.BLUE).a(processingArguments.isUseDefaults()).reset().bold());
        logger.config(
                ansi()
                        .bold()
                        .a(
                                "self-reference-depth {}, large-strings-size {}, random-headers-number {}, limit-fuzzed-fields {}, limit-xxx-of-combinations {}")
                        .reset()
                        .toString(),
                ansi().fg(Ansi.Color.BLUE).a(processingArguments.getSelfReferenceDepth()).reset().bold(),
                ansi().fg(Ansi.Color.BLUE).a(processingArguments.getLargeStringsSize()).reset().bold(),
                ansi().fg(Ansi.Color.BLUE).a(processingArguments.getRandomHeadersNumber()).reset().bold(),
                ansi().fg(Ansi.Color.BLUE).a(processingArguments.getLimitNumberOfFields()).reset().bold(),
                ansi()
                        .fg(Ansi.Color.BLUE)
                        .a(processingArguments.getLimitXxxOfCombinations())
                        .reset()
                        .bold());
        logger.config(
                ansi()
                        .bold()
                        .a(
                                "How the service handles whitespaces and random unicodes: edge-spaces-strategy {}, sanitization-strategy {}")
                        .reset()
                        .toString(),
                ansi().fg(Ansi.Color.BLUE).a(processingArguments.getEdgeSpacesStrategy()).reset().bold(),
                ansi().fg(Ansi.Color.BLUE).a(processingArguments.getSanitizationStrategy()).reset().bold());

        int nofOfOperations = OpenApiUtils.getNumberOfOperations(openAPI);
        logger.config(
                ansi().bold().a("Total number of OpenAPI operations: {}").reset().toString(),
                ansi().fg(Ansi.Color.BLUE).a(nofOfOperations));
    }

    private void fuzzPath(Map.Entry<String, PathItem> pathItemEntry, OpenAPI openAPI) {
        /* WE NEED TO ITERATE THROUGH EACH HTTP OPERATION CORRESPONDING TO THE CURRENT PATH ENTRY*/
        String ansiString = ansi().bold().a("Start fuzzing path {}").reset().toString();
        logger.start(ansiString, pathItemEntry.getKey());
        List<PlaybookData> playbookDataList =
                playbookDataFactory.fromPathItem(pathItemEntry.getKey(), pathItemEntry.getValue(), openAPI);

        if (playbookDataList.isEmpty()) {
            logger.warning(
                    "There was a problem fuzzing path {}. You might want to enable debug mode for more details. Additionally, you can log a GitHub issue at: https://github.com/dochia-dev/dochia-cli/issues.",
                    pathItemEntry.getKey());
            return;
        }

        /* If certain HTTP methods are skipped, we remove the corresponding FuzzingData */
        /* If request uses oneOf/anyOf we only keep the one supplied through --oneOfSelection/--anyOfSelection */
        List<PlaybookData> filteredPlaybookData =
                playbookDataList.stream()
                        .filter(fuzzingData -> filterArguments.isHttpMethodSupplied(fuzzingData.getMethod()))
                        .filter(
                                fuzzingData -> processingArguments.matchesXxxSelection(fuzzingData.getPayload()))
                        .toList();

        Set<HttpMethod> allHttpMethodsFromFuzzingData =
                filteredPlaybookData.stream().map(PlaybookData::getMethod).collect(Collectors.toSet());

        List<TestCasePlaybook> playbooksToRun =
                filterArguments.filterOutPlaybooksNotMatchingHttpMethodsAndPath(
                        allHttpMethodsFromFuzzingData, pathItemEntry.getKey());
        this.runPlaybooks(filteredPlaybookData, playbooksToRun);
        this.runPlaybooks(filteredPlaybookData, filterArguments.getSecondPhasePlaybooks());
    }

    private void runPlaybooks(
            List<PlaybookData> playbookDataListWithHttpMethodsFiltered, List<TestCasePlaybook> configuredTestCasePlaybooks) {
        /*We only run the playbooks supplied and exclude those that do not apply for certain HTTP methods*/

        for (TestCasePlaybook testCasePlaybook : configuredTestCasePlaybooks) {
            List<PlaybookData> filteredData =
                    this.filterFuzzingData(playbookDataListWithHttpMethodsFiltered, testCasePlaybook);
            filteredData.forEach(data -> runSinglePlaybook(testCasePlaybook, data));
        }
    }

    private void runSinglePlaybook(TestCasePlaybook testCasePlaybook, PlaybookData data) {
        logPlaybookStart(testCasePlaybook, data);

        testCaseListener.beforeFuzz(testCasePlaybook.getClass(), data.getContractPath(), data.getMethod().name());
        testCasePlaybook.run(data);
        testCaseListener.afterFuzz(data.getContractPath());

        logPlaybookEnd(testCasePlaybook, data);
    }

    private void logPlaybookEnd(TestCasePlaybook testCasePlaybook, PlaybookData data) {
        logger.complete(
                "Finishing Playbook {}, http method {}, path {}",
                ansi().fgGreen().a(testCasePlaybook.toString()).reset(),
                data.getMethod(),
                data.getPath());
        logger.info("{}", SEPARATOR);
    }

    private void logPlaybookStart(TestCasePlaybook testCasePlaybook, PlaybookData data) {
        logger.start(
                "Starting Playbook {}, http method {}, path {}",
                ansi().fgGreen().a(testCasePlaybook.toString()).reset(),
                data.getMethod(),
                data.getPath());
        logger.debug("Fuzzing payload: {}", data.getPayload());
    }

    private List<PlaybookData> filterFuzzingData(
            List<PlaybookData> playbookDataListWithHttpMethodsFiltered, TestCasePlaybook testCasePlaybook) {
        return CommonUtils.filterAndPrintNotMatching(
                playbookDataListWithHttpMethodsFiltered,
                data -> !testCasePlaybook.skipForHttpMethods().contains(data.getMethod()),
                logger,
                "HTTP method {} is not supported by {}",
                t -> t.getMethod().toString(),
                testCasePlaybook.toString());
    }

    @Override
    public int getExitCode() {
        return exitCodeDueToErrors + executionStatisticsListener.getErrors();
    }
}
