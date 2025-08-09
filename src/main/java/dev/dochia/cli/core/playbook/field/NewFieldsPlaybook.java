package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.io.ServiceData;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Singleton;

import static dev.dochia.cli.core.util.DSLWords.NEW_FIELD;

/**
 * Playbook that adds new fields in requests.
 */
@Singleton
@FieldPlaybook
public class NewFieldsPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(NewFieldsPlaybook.class);
    private final ServiceCaller serviceCaller;
    private final TestCaseListener testCaseListener;

    /**
     * Creates a new NewFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     */
    public NewFieldsPlaybook(ServiceCaller sc, TestCaseListener lr) {
        this.serviceCaller = sc;
        this.testCaseListener = lr;
    }

    @Override
    public void run(PlaybookData data) {
        testCaseListener.createAndExecuteTest(logger, this, () -> process(data), data);
    }

    private void process(PlaybookData data) {
        String fuzzedJson = this.addNewField(data);
        if (JsonUtils.equalAsJson(fuzzedJson, data.getPayload())) {
            testCaseListener.skipTest(logger, "Could not fuzz the payload");
            return;
        }

        ResponseCodeFamily expectedResultCode = ResponseCodeFamilyPredefined.TWOXX;
        if (HttpMethod.requiresBody(data.getMethod())) {
            expectedResultCode = ResponseCodeFamilyPredefined.FOURXX;
        }
        testCaseListener.addScenario(logger, "Add new field inside the request: name [{}], value [{}]. All other details are similar to a happy flow", NEW_FIELD, NEW_FIELD);
        testCaseListener.addExpectedResult(logger, "Should get a [{}] response code", expectedResultCode.asString());

        HttpResponse response = serviceCaller.call(ServiceData.builder().relativePath(data.getPath()).headers(data.getHeaders())
                .payload(fuzzedJson).queryParams(data.getQueryParams()).httpMethod(data.getMethod()).contractPath(data.getContractPath())
                .contentType(data.getFirstRequestContentType()).pathParamsPayload(data.getPathParamsPayload())
                .build());
        testCaseListener.reportResult(logger, data, response, expectedResultCode);
    }

    String addNewField(PlaybookData data) {
        if (JsonUtils.isEmptyPayload(data.getPayload())) {
            return "{\"" + NEW_FIELD + "\":\"" + NEW_FIELD + "\"}";
        }

        JsonElement jsonElement = JsonParser.parseString(data.getPayload());

        if (jsonElement instanceof JsonObject jsonObject) {
            jsonObject.addProperty(NEW_FIELD, NEW_FIELD);
        } else if (jsonElement instanceof JsonArray jsonArray) {
            for (JsonElement element : jsonArray) {
                if (element instanceof JsonObject jsonObject) {
                    jsonObject.addProperty(NEW_FIELD, NEW_FIELD);
                }
            }
        }

        return jsonElement.toString();
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public String description() {
        return "send a happy flow request and add a new field inside the request called 'dochiaFuzzyField'";
    }
}
