package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.io.ServiceData;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.JsonUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
@FieldPlaybook
public class ZeroWidthCharsInNamesFieldsPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(ZeroWidthCharsInNamesFieldsPlaybook.class);
    private final ServiceCaller serviceCaller;
    private final TestCaseListener testCaseListener;

    /**
     * Creates a new ZeroWidthCharsInNamesFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     */
    public ZeroWidthCharsInNamesFieldsPlaybook(ServiceCaller sc, TestCaseListener lr) {
        this.serviceCaller = sc;
        this.testCaseListener = lr;
    }

    @Override
    public void run(PlaybookData data) {
        if (JsonUtils.isEmptyPayload(data.getPayload())) {
            logger.debug("Skip playbook as payload is empty");
            return;
        }
        for (String fuzzValue : UnicodeGenerator.getZwCharsSmallListFields()) {
            for (String fuzzedField : data.getAllFieldsByHttpMethod()
                    .stream()
                    .filter(field -> JsonUtils.isFieldInJson(data.getPayload(), field))
                    .limit(5)
                    .collect(Collectors.toSet())) {
                testCaseListener.createAndExecuteTest(logger, this, () -> process(data, fuzzedField, fuzzValue), data);
            }
        }
    }

    private void process(PlaybookData data, String fuzzedField, String fuzzValue) {
        String fuzzedPayload = getFuzzedPayload(data, fuzzedField, fuzzValue);

        testCaseListener.addScenario(logger, "Insert zero-width chars in field names: field [{}], char [{}]. All other details are similar to a happy flow",
                fuzzedField, FuzzingStrategy.formatValue(fuzzValue));
        testCaseListener.addExpectedResult(logger, "Should get a [{}] response code", ResponseCodeFamilyPredefined.FOURXX.asString());

        HttpResponse response = serviceCaller.call(ServiceData.builder().relativePath(data.getPath()).headers(data.getHeaders())
                .payload(fuzzedPayload).queryParams(data.getQueryParams()).httpMethod(data.getMethod()).contractPath(data.getContractPath())
                .contentType(data.getFirstRequestContentType()).pathParamsPayload(data.getPathParamsPayload())
                .build());
        testCaseListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.FOURXX);
    }

    private static @NotNull String getFuzzedPayload(PlaybookData data, String fuzzedField, String fuzzValue) {
        String currentPayload = data.getPayload();
        String simpleFuzzedFieldName = fuzzedField.substring(fuzzedField.lastIndexOf("#") + 1);
        String fuzzedFieldName = CommonUtils.insertInTheMiddle(simpleFuzzedFieldName, fuzzValue, true);
        return currentPayload.replace("\"" + simpleFuzzedFieldName + "\"", "\"" + fuzzedFieldName + "\"");
    }

    @Override
    public List<HttpMethod> skipForHttpMethods() {
        return List.of(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.TRACE);
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public String description() {
        return "iterate through each field and insert zero-width characters in the field names";
    }
}
