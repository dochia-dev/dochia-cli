package dev.dochia.cli.core.command;

import dev.dochia.cli.core.args.ApiArguments;
import dev.dochia.cli.core.args.AuthArguments;
import dev.dochia.cli.core.args.CheckArguments;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.args.IgnoreArguments;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.args.QualityGateArguments;
import dev.dochia.cli.core.args.ReportingArguments;
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
import dev.dochia.cli.core.util.AnsiUtils;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.DochiaRandom;
import dev.dochia.cli.core.util.OpenApiUtils;
import dev.dochia.cli.core.util.VersionChecker;
import dev.dochia.cli.core.util.VersionProvider;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.quarkus.arc.Unremovable;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import jakarta.inject.Inject;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        abbreviateSynopsis = true,
        synopsisHeading = "@|bold,underline Usage:|@%n",
        customSynopsis = {
                "@|bold dochia|@ @|fg(yellow) test -c|@ <contract> @|fg(yellow) -s|@ <server> [ADDITIONAL OPTIONS]",
                ""
        },
        exitCodeListHeading = "%n@|bold,underline Exit Codes:|@%n",
        exitCodeList = {
                "@|bold  0|@:Successful program execution",
                "@|bold 2|@:Usage error: user input for the command was incorrect",
                "@|bold 1|@:Internal execution error: an exception occurred when executing command"},
        footerHeading = "%n@|bold,underline Examples:|@%n",
        footer = {
                "  Run in blackbox mode and only report 500 http error codes:",
                "    dochia test -c openapi.yml -s http://localhost:8080 -b",
                "",
                "  Run with authentication headers from an environment variable called TOKEN:",
                "    dochia test -c openapi.yml -s http://localhost:8080 -H API-Token=$$TOKEN"
        })
@Unremovable
public class TestCommand implements Runnable, CommandLine.IExitCodeGenerator, AutoCloseable {

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

    @Inject
    @CommandLine.ArgGroup(heading = "%n@|bold,underline Quality Gate Options:|@%n", exclusive = false)
    QualityGateArguments qualityGateArguments;


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

    private int exitCodeDueToErrors = CommandLine.ExitCode.OK;

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
            exitCodeDueToErrors = CommandLine.ExitCode.SOFTWARE;
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
                    AnsiUtils.boldBrightBlue("A new version is available: {}. Download url: {}");
            String currentVersionFormatted =
                    AnsiUtils.boldBrightBlue(checkResult.getVersion());
            String downloadUrlFormatted =
                    AnsiUtils.boldGreen(checkResult.getDownloadUrl());
            logger.star(message, currentVersionFormatted, downloadUrlFormatted);
        }
    }

    private void doLogic() throws IOException {
        filterArguments.applyProfile(spec);
        this.prepareRun();
        OpenAPI openAPI = this.createOpenAPI();
        this.checkOpenAPI(openAPI);
        apiArguments.validateValidServer(spec, openAPI);
        filterArguments.validateValidPaths(openAPI);
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
                    AnsiUtils.boldYellow(
                            "There were {} tests failing with authorisation errors. Either supply authentication details or check if the supplied credentials are correct!");
            logger.star(message, executionStatisticsListener.getAuthErrors());
        }
        if (executionStatisticsListener.areManyIoErrors()) {
            String message =
                    AnsiUtils.boldYellow(
                            "There were {} tests failing with i/o errors. Make sure that you have access to the service or that the --server url is correct!");
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
        String finishMessage = AnsiUtils.green("Finished parsing the contract in {} ms");
        long t0 = System.currentTimeMillis();
        OpenAPI openAPI = OpenApiUtils.readOpenApi(apiArguments.getContract());
        logger.debug(finishMessage, (System.currentTimeMillis() - t0));
        return openAPI;
    }

    void prepareRun() throws IOException {
        // this is a hack to set terminal width here in order to avoid importing a full-blown library
        // like jline
        // just for getting the terminal width
        CommonUtils.initRandom(processingArguments.getSeed());
        ConsoleUtils.initTerminalWidth(spec);
        reportingArguments.processLogData();
        apiArguments.validateRequired(spec);
        filesArguments.loadConfig();
    }

    private void printConfiguration(OpenAPI openAPI) {
        logger.config(AnsiUtils.bold("OpenAPI specs: {}"), AnsiUtils.blue(apiArguments.getContract()));
        logger.config(AnsiUtils.bold("API base url: {}"), AnsiUtils.blue(apiArguments.getServer()));
        logger.config(AnsiUtils.bold("Reporting path: {}"), AnsiUtils.blue(reportingArguments.getOutputReportFolder()));
        logger.config(AnsiUtils.bold("Reporting path: {}"), AnsiUtils.blue(reportingArguments.getOutputReportFolder()));
        logger.config(AnsiUtils.bold("{} configured playbooks out of {} total playbooks"),
                AnsiUtils.blue(filterArguments.getFirstPhasePlaybooksForPath().size()),
                AnsiUtils.blue(filterArguments.getTotalPlaybooks()));
        logger.config(AnsiUtils.bold("{} configured paths out of {} total OpenAPI paths"),
                AnsiUtils.blue(filterArguments.getPathsToRun(openAPI).size()),
                AnsiUtils.blue(openAPI.getPaths().size()));
        logger.config(AnsiUtils.bold("HTTP methods in scope: {}"),
                AnsiUtils.blue(filterArguments.getHttpMethods()));
        logger.config(AnsiUtils.bold("Example flags: use-request-body-examples {}, use-schema-examples {}, use-property-examples {}, use-response-body-examples {}, use-defaults {}"),
                AnsiUtils.blue(processingArguments.isUseRequestBodyExamples()),
                AnsiUtils.blue(processingArguments.isUseSchemaExamples()),
                AnsiUtils.blue(processingArguments.isUsePropertyExamples()),
                AnsiUtils.blue(processingArguments.isUseResponseBodyExamples()),
                AnsiUtils.blue((processingArguments.isUseDefaults())));
        logger.config(AnsiUtils.bold("self-reference-depth {}, large-strings-size {}, random-headers-number {}, limit-fuzzed-fields {}, limit-xxx-of-combinations {}"),
                AnsiUtils.blue(processingArguments.getSelfReferenceDepth()),
                AnsiUtils.blue(processingArguments.getLargeStringsSize()),
                AnsiUtils.blue(processingArguments.getRandomHeadersNumber()),
                AnsiUtils.blue(processingArguments.getLimitNumberOfFields()),
                AnsiUtils.blue((processingArguments.getLimitXxxOfCombinations())));
        logger.config(AnsiUtils.bold("How the service handles whitespaces and random unicodes: edge-spaces-strategy {}, sanitization-strategy {}"),
                AnsiUtils.blue(processingArguments.getEdgeSpacesStrategy()),
                AnsiUtils.blue(processingArguments.getSanitizationStrategy()));
        logger.config(AnsiUtils.bold("Seed value: {}"), AnsiUtils.blue(DochiaRandom.getStoredSeed()));
        logger.config(AnsiUtils.bold("Quality gate: {}"),
                AnsiUtils.boldBlue(qualityGateArguments.getQualityGateDescription()));

        int nofOfOperations = OpenApiUtils.getNumberOfOperations(openAPI);
        logger.config(AnsiUtils.bold("Total number of OpenAPI operations: {}"), AnsiUtils.blue(nofOfOperations));
    }

    private void fuzzPath(Map.Entry<String, PathItem> pathItemEntry, OpenAPI openAPI) {
        /* WE NEED TO ITERATE THROUGH EACH HTTP OPERATION CORRESPONDING TO THE CURRENT PATH ENTRY*/
        String ansiString = AnsiUtils.bold("Start fuzzing path {}");
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
        if (data.shouldSkipPlaybookForPath(testCasePlaybook.toString())) {
            logger.skip("Skipping Playbook {} for path {} due to OpenAPI extension configuration",
                    AnsiUtils.yellow(testCasePlaybook.toString()), data.getPath());
            return;
        }
        logPlaybookStart(testCasePlaybook, data);

        testCaseListener.beforeFuzz(testCasePlaybook.getClass(), data.getContractPath(), data.getMethod().name());
        testCasePlaybook.run(data);
        testCaseListener.afterFuzz(data.getContractPath());

        logPlaybookEnd(testCasePlaybook, data);
    }

    private void logPlaybookEnd(TestCasePlaybook testCasePlaybook, PlaybookData data) {
        logger.complete(
                "Finishing Playbook {}, http method {}, path {}",
                AnsiUtils.green(testCasePlaybook.toString()),
                data.getMethod(),
                data.getPath());
        logger.info("{}", SEPARATOR);
    }

    private void logPlaybookStart(TestCasePlaybook testCasePlaybook, PlaybookData data) {
        logger.start(
                "Starting Playbook {}, http method {}, path {}",
                AnsiUtils.green(testCasePlaybook.toString()),
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
        if (exitCodeDueToErrors > 0) {
            return exitCodeDueToErrors;
        }

        long errors = executionStatisticsListener.getErrors();
        long warnings = executionStatisticsListener.getWarns();

        if (qualityGateArguments.shouldFailBuild(errors, warnings)) {
            logger.debug("Build failed due to quality gate: {}", qualityGateArguments.getQualityGateDescription());
            return CommandLine.ExitCode.SOFTWARE;
        }

        return CommandLine.ExitCode.OK;
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException _) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
