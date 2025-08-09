package dev.dochia.cli.core.playbook.special;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.api.SpecialPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.MatchArguments;
import dev.dochia.cli.core.args.StopArguments;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.playbook.special.mutators.api.CustomMutator;
import dev.dochia.cli.core.playbook.special.mutators.api.CustomMutatorConfig;
import dev.dochia.cli.core.playbook.special.mutators.api.CustomMutatorKeywords;
import dev.dochia.cli.core.playbook.special.mutators.api.Mutator;
import dev.dochia.cli.core.report.ExecutionStatisticsListener;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.JsonUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

/**
 * Playbook intended for continuous fuzzing. It will randomly choose fields to fuzz and mutators to apply.
 * The Playbook will stop after one of the supplied stopXXX conditions is met: time elapsed, errors occurred or tests executed.
 */
@Singleton
@SpecialPlaybook
public class RandomPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(RandomPlaybook.class);
    private final SimpleExecutor simpleExecutor;
    private final TestCaseListener testCaseListener;
    private final ExecutionStatisticsListener executionStatisticsListener;
    private final MatchArguments matchArguments;
    private final StopArguments stopArguments;
    private final FilesArguments filesArguments;
    private final Instance<Mutator> mutators;

    @Inject
    public RandomPlaybook(SimpleExecutor simpleExecutor, TestCaseListener testCaseListener,
                          ExecutionStatisticsListener executionStatisticsListener,
                          MatchArguments matchArguments, Instance<Mutator> mutators,
                          StopArguments stopArguments, FilesArguments filesArguments) {
        this.simpleExecutor = simpleExecutor;
        this.testCaseListener = testCaseListener;
        this.executionStatisticsListener = executionStatisticsListener;
        this.matchArguments = matchArguments;
        this.mutators = mutators;
        this.stopArguments = stopArguments;
        this.filesArguments = filesArguments;
    }

    @Override
    public void run(PlaybookData data) {
        if (JsonUtils.isEmptyPayload(data.getPayload())) {
            logger.error("Skipping playbook as payload is empty");
            return;
        }
        List<Mutator> mutatorsToRun = this.getMutators();

        if (mutatorsToRun.isEmpty()) {
            logger.error("No Mutators to run! Enable debug for more details.");
            return;
        }

        long startTime = System.currentTimeMillis();

        boolean shouldStop = false;
        Set<String> allFields = data.getAllFieldsByHttpMethod();

        testCaseListener.updateUnknownProgress(data);

        while (!shouldStop) {
            String targetField = CommonUtils.selectRandom(allFields);
            logger.debug("Selected field to be mutated: [{}]", targetField);

            if (!JsonUtils.isFieldInJson(data.getPayload(), targetField)) {
                logger.debug("Field not in this payload, selecting another one...");
                continue;
            }

            Mutator selectedRandomMutator = CommonUtils.selectRandom(mutatorsToRun);
            logger.debug("Selected mutator [{}]", selectedRandomMutator.getClass().getSimpleName());

            String mutatedPayload = selectedRandomMutator.mutate(data.getPayload(), targetField);
            Collection<DochiaHeader> mutatedHeaders = selectedRandomMutator.mutate(data.getHeaders());

            simpleExecutor.execute(
                    SimpleExecutorContext.builder()
                            .testCasePlaybook(this)
                            .playbookData(data)
                            .logger(logger)
                            .payload(mutatedPayload)
                            .headers(mutatedHeaders)
                            .scenario("Send a random payload mutating field [%s] with [%s] mutator".formatted(targetField, selectedRandomMutator.description()))
                            .expectedSpecificResponseCode("a response that doesn't match given --matchXXX arguments")
                            .responseProcessor(this::processResponse)
                            .build());

            testCaseListener.updateUnknownProgress(data);
            shouldStop = stopArguments.shouldStop(executionStatisticsListener.getErrors(), testCaseListener.getCurrentTestCaseNumber(), startTime);
        }
    }

    void processResponse(HttpResponse httpResponse, PlaybookData playbookData) {
        if (matchArguments.isMatchResponse(httpResponse)) {
            testCaseListener.reportResultError(logger, playbookData, "Response matches arguments", "Response matches" + matchArguments.getMatchString());
        } else {
            testCaseListener.skipTest(logger, "Skipping test as response does not match given matchers!");
        }
    }

    private List<Mutator> getMutators() {
        if (filesArguments.getMutatorsFolder() == null) {
            return mutators.stream().toList();
        }

        return this.parseMutators();
    }

    private List<Mutator> parseMutators() {
        List<Mutator> customMutators = new ArrayList<>();

        File mutatorsFolder = filesArguments.getMutatorsFolder();
        File[] customMutatorsFiles = mutatorsFolder.listFiles();

        if (customMutatorsFiles == null) {
            logger.error("Invalid custom Mutators folder {}", filesArguments.getMutatorsFolder().getAbsolutePath());
            return Collections.emptyList();
        }

        for (File customMutatorFile : Objects.requireNonNull(customMutatorsFiles)) {
            try {
                Map<String, Object> customMutator = parseYamlAsSimpleMap(customMutatorFile.getCanonicalPath());

                CustomMutatorConfig config = this.createConfig(customMutator);
                customMutators.add(new CustomMutator(config));
            } catch (Exception e) {
                logger.warn("There was a problem parsing {}: {}", customMutatorFile.getAbsolutePath(), e.toString());
            }
        }

        return List.copyOf(customMutators);
    }

    CustomMutatorConfig createConfig(Map<String, Object> customMutator) throws IOException {
        String name = String.valueOf(
                customMutator.get(
                        CustomMutatorKeywords.NAME.name().toLowerCase(Locale.ROOT)
                )
        );

        CustomMutatorConfig.Type type = CustomMutatorConfig.Type.valueOf(
                String.valueOf(
                        customMutator.get(
                                CustomMutatorKeywords.TYPE.name().toLowerCase(Locale.ROOT)
                        )
                ).toUpperCase(Locale.ROOT)
        );
        Object customMutatorValues = customMutator.get(CustomMutatorKeywords.VALUES.name().toLowerCase(Locale.ROOT));
        List<?> values;

        if (customMutatorValues instanceof List<?> valuesList) {
            logger.debug("Custom playbook values from mutator file");
            values = valuesList;
        } else {
            String fileLocation = String.valueOf(customMutatorValues);
            logger.debug("Loading custom playbook values from external file {}", fileLocation);
            values = readValueFromFile(fileLocation);
        }

        return new CustomMutatorConfig(name, type, values);
    }

    List<String> readValueFromFile(String fileLocation) throws IOException {
        return Files.readAllLines(Path.of(fileLocation))
                .stream()
                .filter(Predicate.not(String::isBlank))
                .filter(Predicate.not(line -> line.startsWith("#")))
                .toList();
    }

    static Map<String, Object> parseYamlAsSimpleMap(String yaml) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (Reader reader = new InputStreamReader(new FileInputStream(yaml), StandardCharsets.UTF_8)) {
            JsonNode node = mapper.reader().readTree(reader);
            return mapper.convertValue(node, new TypeReference<>() {
            });
        }
    }

    @Override
    public String description() {
        return "continuously fuzz random fields with random values based on registered mutators";
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }
}
