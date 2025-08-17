package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.io.ServiceData;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.report.TestCaseListener;
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
    private final ServiceCaller serviceCaller;
    private final TestCaseListener testCaseListener;
    private final FilterArguments filterArguments;
    private final ProcessingArguments processingArguments;


    /**
     * Creates a new RemoveFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param fa filter arguments
     * @param pa to get the number of max fields to remove at once
     */
    public RemoveFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilterArguments fa, ProcessingArguments pa) {
        this.serviceCaller = sc;
        this.testCaseListener = lr;
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
                testCaseListener.createAndExecuteTest(logger, this, () -> process(data, data.getAllRequiredFields(), finalSubset), data);
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
            testCaseListener.addScenario(logger, "Remove the following fields from request: {}", subset);

            boolean hasRequiredFieldsRemove = this.hasRequiredFieldsRemove(required, subset);
            testCaseListener.addExpectedResult(logger, "Should return [{}] response code as required fields [{}] removed", ResponseCodeFamilyPredefined.getExpectedWordingBasedOnRequiredFields(hasRequiredFieldsRemove));

            HttpResponse response = serviceCaller.call(ServiceData.builder().relativePath(data.getPath()).headers(data.getHeaders())
                    .payload(finalJsonPayload).queryParams(data.getQueryParams()).httpMethod(data.getMethod()).contractPath(data.getContractPath())
                    .contentType(data.getFirstRequestContentType()).pathParamsPayload(data.getPathParamsPayload()).build());
            testCaseListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.getResultCodeBasedOnRequiredFieldsRemoved(hasRequiredFieldsRemove));
        } else {
            testCaseListener.skipTest(logger, "Field is from a different ANY_OF or ONE_OF payload");
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
