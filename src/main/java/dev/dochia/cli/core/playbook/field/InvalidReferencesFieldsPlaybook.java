package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.WordUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Playbook that sends invalid references in fields.
 */
@Singleton
@FieldPlaybook
public class InvalidReferencesFieldsPlaybook implements TestCasePlaybook {
    private static final Pattern VARIABLES_PATTERN = Pattern.compile("\\{([^{]*)}");
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(InvalidReferencesFieldsPlaybook.class);

    private final FilesArguments filesArguments;
    private final SimpleExecutor simpleExecutor;
    private final TestCaseListener testCaseListener;

    /**
     * Creates a new InvalidReferencesFieldsPlaybook instance.
     *
     * @param filesArguments   files arguments
     * @param simpleExecutor   the executor
     * @param testCaseListener the test case listener
     */
    public InvalidReferencesFieldsPlaybook(FilesArguments filesArguments, SimpleExecutor simpleExecutor, TestCaseListener testCaseListener) {
        this.filesArguments = filesArguments;
        this.simpleExecutor = simpleExecutor;
        this.testCaseListener = testCaseListener;
    }

    @Override
    public void run(PlaybookData data) {
        if (this.hasPathVariables(data.getPath())) {
            List<String> pathCombinations = this.replacePathVariables(data.getPath());
            for (String pathCombination : pathCombinations) {
                simpleExecutor.execute(
                        SimpleExecutorContext.builder()
                                .testCasePlaybook(this)
                                .playbookData(data)
                                .logger(logger)
                                .path(pathCombination)
                                .scenario("Fuzz path parameters for HTTP methods with bodies. Current path: %s".formatted(pathCombination))
                                .expectedSpecificResponseCode("[2XX, 4XX]")
                                .responseProcessor(this::processResponse)
                                .build());
            }
        }
    }

    private void processResponse(HttpResponse httpResponse, PlaybookData playbookData) {
        if (ResponseCodeFamily.is4xxCode(httpResponse.getResponseCode()) || ResponseCodeFamily.is2xxCode(httpResponse.getResponseCode())) {
            testCaseListener.reportResultInfo(logger, playbookData, "Response code expected: [{}]", httpResponse.getResponseCode());
        } else {
            testCaseListener.reportResultError(logger, playbookData,
                    "Unexpected response code: %s".formatted(httpResponse.responseCodeAsString()),
                    "Request failed unexpectedly for http method [{}]: expected [{}], actual [{}]",
                    httpResponse.getHttpMethod(), "4XX, 2XX", httpResponse.responseCodeAsString());
        }
    }

    private List<String> replacePathVariables(String path) {
        List<String> variables = new ArrayList<>();
        Matcher matcher = VARIABLES_PATTERN.matcher(path);
        while (matcher.find()) {
            variables.add(matcher.group());
        }

        Map<String, String> variablesValues = new HashMap<>();
        for (String variable : variables) {
            String variableName = variable.substring(1, variable.length() - 1);
            String value = Optional.ofNullable(filesArguments.getRefData(path).get(variableName))
                    .map(WordUtils::nullOrValueOf)
                    .orElse(filesArguments.getUrlParamsList()
                            .stream()
                            .filter(param -> param.startsWith(variableName + ":"))
                            .map(item -> item.split(":", -1)[1])
                            .findFirst().orElse(variable));
            variablesValues.put(variable, value);
        }

        return createPathCombinations(path, variables, variablesValues);
    }

    private static List<String> createPathCombinations(String path, List<String> variables, Map<String, String> variablesValues) {
        List<String> result = new ArrayList<>();
        List<String> payloads = new ArrayList<>(UnicodeGenerator.getAbugidasChars());
        payloads.add(UnicodeGenerator.getZalgoText());
        payloads.addAll(UnicodeGenerator.getInvalidReferences());

        for (String variable : variables) {
            String interimPath = path;
            for (Map.Entry<String, String> entry : variablesValues.entrySet()) {
                if (!entry.getKey().equalsIgnoreCase(variable)) {
                    interimPath = interimPath.replace(entry.getKey(), entry.getValue());
                }
            }
            for (String payload : payloads) {
                String fuzzedPath = interimPath.replace(variable, variablesValues.get(variable) + payload);
                result.add(fuzzedPath);
            }
        }

        return result;
    }

    private boolean hasPathVariables(String path) {
        return path.contains("{");
    }

    @Override
    public String description() {
        return "Iterate through each path parameter and send invalid reference values";
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }
}
