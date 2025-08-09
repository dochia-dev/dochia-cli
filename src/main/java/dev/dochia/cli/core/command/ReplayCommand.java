package dev.dochia.cli.core.command;

import dev.dochia.cli.core.args.AuthArguments;
import dev.dochia.cli.core.command.model.HelpFullOption;
import dev.dochia.cli.core.dsl.DSLParser;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.util.JsonUtils;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.TestCase;
import dev.dochia.cli.core.util.KeyValuePair;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.VersionProvider;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.quarkus.arc.Unremovable;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        exitCodeOnInvalidInput = 191,
        exitCodeOnExecutionException = 192,
        description = "Replay previously executed dochia tests.",
        exitCodeListHeading = "%n@|bold,underline Exit Codes:|@%n",
        exitCodeList = {"@|bold  0|@:Successful program execution",
                "@|bold 191|@:Usage error: user input for the command was incorrect",
                "@|bold 192|@:Internal execution error: an exception occurred when executing command"},
        footerHeading = "%n@|bold,underline Examples:|@%n",
        footer = {"  Replay Test 1 from the default reporting folder:",
                "    dochia replay Test1",
                "", "  Replay Test 1 from the default reporting folder and write the new output in another folder",
                "    dochia replay Test1 --output path/to/new/folder"},
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

    @CommandLine.Option(names = {"-D", "--debug"},
            description = "Sets log level to ALL. Useful for diagnosing when raising bugs")
    private boolean debug;

    @CommandLine.Option(names = {"-H"},
            description = "Specifies the headers to be passed with all the re-played tests. It will override values from the replay files for the same header name")
    Map<String, Object> headersMap = new HashMap<>();

    @CommandLine.Option(names = {"-s", "--server"},
            description = "Base URL of the service. It can be used to overwrite the base URL from the initial test in order to replay it against other service instances")
    private String server;

    @CommandLine.Option(names = {"-o", "--output"},
            description = "If supplied, it will create TestXXX.json files within the given folder with the updated responses received when replaying the tests")
    private String outputReportFolder;


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
        return Arrays.stream(tests)
                .map(testCase -> testCase.trim().strip())
                .map(testCase -> testCase.endsWith(".json") ? testCase : "dochia-report/" + testCase + ".json")
                .toList();
    }

    private void executeTestCase(String testCaseFileName) throws IOException {
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

        logger.complete("Response body: \n{}", response.getBody());
        this.writeTestJsonsIfSupplied(testCase, response);
        this.showResponseCodesDifferences(testCase, response);
    }

    void showResponseCodesDifferences(TestCase testCase, HttpResponse response) {
        logger.noFormat("");
        logger.star("Old response code: {}", testCase.getResponse().getResponseCode());
        logger.star("New response code: {}", response.getResponseCode());

        logger.noFormat("");
        logger.star("Old response body: {}", testCase.getResponse().getJsonBody());
        logger.star("New response body: {}", response.getJsonBody());
        logger.noFormat("");
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

        //remove old headers
        headersFromFile.removeIf(header -> headersMap.containsKey(header.getKey()));

        //add new headers
        headersFromFile.addAll(headersMap.entrySet().stream().map(entry -> new KeyValuePair<>(entry.getKey(), entry.getValue())).toList());

        //see if any header is dynamic and it needs a parser
        headersFromFile.forEach(header -> header.setValue(DSLParser.parseAndGetResult(header.getValue().toString(), authArgs.getAuthScriptAsMap())));
    }

    private TestCase loadTestCaseFile(String testCaseFileName) throws IOException {
        String testCaseFile = Files.readString(Paths.get(testCaseFileName));
        logger.config("Loaded content: \n" + testCaseFile);
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
        if (debug) {
            CommonUtils.setDochiaLogLevel("ALL");
            logger.fav("Setting dochia log level to ALL!");
        }
        this.initReportingPath();
        for (String testCaseFileName : this.parseTestCases()) {
            try {
                logger.start("Executing {}", testCaseFileName);
                this.executeTestCase(testCaseFileName);
                logger.complete("Finish executing {}", testCaseFileName);
            } catch (IOException e) {
                logger.debug("Exception while replaying test!", e);
                logger.error("Something went wrong while replaying {}. If the test name ends with .json it is searched as a full path. " +
                        "If it doesn't have an extension it will be searched in dochia-report/ folder. Error message: {}", testCaseFileName, e.toString());
            }
        }
    }
}
