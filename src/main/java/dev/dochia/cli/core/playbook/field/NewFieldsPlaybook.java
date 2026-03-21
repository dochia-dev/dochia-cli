package dev.dochia.cli.core.playbook.field;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamily;
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

import static dev.dochia.cli.core.util.DSLWords.NEW_FIELD;

/**
 * Playbook that adds new fields in requests.
 */
@Singleton
@FieldPlaybook
public class NewFieldsPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(NewFieldsPlaybook.class);
    private final SimpleExecutor simpleExecutor;

    /**
     * Creates a new NewFieldsPlaybook instance.
     *
     * @param simpleExecutor the simple executor
     */
    public NewFieldsPlaybook(SimpleExecutor simpleExecutor) {
        this.simpleExecutor = simpleExecutor;
    }

    @Override
    public void run(PlaybookData data) {
        String fuzzedJson = this.addNewField(data);
        if (JsonUtils.equalAsJson(fuzzedJson, data.getPayload())) {
            logger.skip("Could not add new field to the payload. Skipping fuzzing");
            return;
        }

        ResponseCodeFamily expectedResultCode = ResponseCodeFamilyPredefined.TWOXX;
        if (HttpMethod.requiresBody(data.getMethod())) {
            expectedResultCode = ResponseCodeFamilyPredefined.FOURXX;
        }

        simpleExecutor.execute(SimpleExecutorContext.builder()
                .logger(logger)
                .testCasePlaybook(this)
                .playbookData(data)
                .payload(fuzzedJson)
                .expectedResponseCode(expectedResultCode)
                .scenario("Add new field inside the request: name [" + NEW_FIELD + "], value [" + NEW_FIELD + "]. All other details are similar to a happy flow")
                .build());
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
        return " Send a happy path request and add a new field 'dochiaFuzzyField'";
    }
}
