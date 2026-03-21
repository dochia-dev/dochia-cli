package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.JsonUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Playbook at fields level. It will remove different fields from the payload based on multiple strategies.
 */
@Singleton
@FieldPlaybook
public class RemoveFieldsPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(RemoveFieldsPlaybook.class);
    private final SimpleExecutor simpleExecutor;
    private final FilterArguments filterArguments;
    private final ProcessingArguments processingArguments;


    /**
     * Creates a new RemoveFieldsPlaybook instance.
     *
     * @param simpleExecutor the simple executor
     * @param fa             filter arguments
     * @param pa             to get the number of max fields to remove at once
     */
    public RemoveFieldsPlaybook(SimpleExecutor simpleExecutor, FilterArguments fa, ProcessingArguments pa) {
        this.simpleExecutor = simpleExecutor;
        this.filterArguments = fa;
        this.processingArguments = pa;
    }

    @Override
    public void run(PlaybookData data) {
        logger.debug("All required fields, including subfields: {}", data.getAllRequiredFields());
        Set<Set<String>> sets = this.getAllFields(data);

        for (Set<String> subset : sets) {
            Set<String> finalSubset = this.removeIfSkipped(subset);
            if (!finalSubset.isEmpty()) {
                process(data, data.getAllRequiredFields(), finalSubset);
            }
        }
    }

    private Set<String> removeIfSkipped(Set<String> subset) {
        return subset.stream()
                .filter(field -> !filterArguments.getSkipFields().contains(field))
                .collect(Collectors.toSet());
    }

    private Set<Set<String>> getAllFields(PlaybookData data) {
        Set<Set<String>> sets = data.getAllFields(PlaybookData.SetFuzzingStrategy.valueOf(processingArguments.getFieldsSelectionStrategy().name())
                , processingArguments.getMaxFieldsToRemove());

        logger.note("Playbook will run with [{}] fields configuration possibilities out of [{}] maximum possible",
                sets.size(), (int) Math.pow(2, data.getAllFieldsByHttpMethod().size()));

        return sets;
    }


    private void process(PlaybookData data, List<String> required, Set<String> subset) {
        logger.debug("Payload {} and fields to remove {}", data.getPayload(), subset);
        String finalJsonPayload = this.getFuzzedJsonWithFieldsRemove(data.getPayload(), subset);

        if (!JsonUtils.equalAsJson(finalJsonPayload, data.getPayload())) {
            boolean hasRequiredFieldsRemove = this.hasRequiredFieldsRemove(required, subset);
            Object[] expectedWording = ResponseCodeFamilyPredefined.getExpectedWordingBasedOnRequiredFields(hasRequiredFieldsRemove);

            simpleExecutor.execute(SimpleExecutorContext.builder()
                    .logger(logger)
                    .testCasePlaybook(this)
                    .playbookData(data)
                    .payload(finalJsonPayload)
                    .expectedResponseCode(ResponseCodeFamilyPredefined.from(String.valueOf(expectedWording[0])))
                    .scenario("Remove the following fields from request: " + subset.toString())
                    .expectedResult(String.format(" as required fields %s removed", expectedWording[1]))
                    .build());
        } else {
            logger.skip("Field is from a different ANY_OF or ONE_OF payload. Skipping test");
        }
    }

    private boolean hasRequiredFieldsRemove(List<String> required, Set<String> subset) {
        Set<String> intersection = new HashSet<>(required);
        intersection.retainAll(subset);
        return !intersection.isEmpty();
    }


    private String getFuzzedJsonWithFieldsRemove(String payload, Set<String> fieldsToRemove) {
        String prefix = "";

        if (JsonUtils.isJsonArray(payload)) {
            prefix = JsonUtils.ALL_ELEMENTS_ROOT_ARRAY;
        }
        for (String field : fieldsToRemove) {
            payload = JsonUtils.deleteNode(payload, prefix + field);
        }

        return payload;
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public List<HttpMethod> skipForHttpMethods() {
        return Arrays.asList(HttpMethod.GET, HttpMethod.DELETE);
    }

    @Override
    public String description() {
        return "Iterate through each request field and remove fields according to the configured strategy";
    }
}
