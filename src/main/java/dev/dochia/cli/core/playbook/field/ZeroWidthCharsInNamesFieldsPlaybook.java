package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
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
    private final SimpleExecutor simpleExecutor;

    /**
     * Creates a new ZeroWidthCharsInNamesFieldsPlaybook instance.
     *
     * @param simpleExecutor the simple executor
     */
    public ZeroWidthCharsInNamesFieldsPlaybook(SimpleExecutor simpleExecutor) {
        this.simpleExecutor = simpleExecutor;
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
                process(data, fuzzedField, fuzzValue);
            }
        }
    }

    private void process(PlaybookData data, String fuzzedField, String fuzzValue) {
        String fuzzedPayload = getFuzzedPayload(data, fuzzedField, fuzzValue);

        simpleExecutor.execute(SimpleExecutorContext.builder()
                .logger(logger)
                .testCasePlaybook(this)
                .playbookData(data)
                .payload(fuzzedPayload)
                .expectedResponseCode(ResponseCodeFamilyPredefined.FOURXX)
                .scenario("Insert zero-width chars in field names: field [" + fuzzedField + "], char [" + FuzzingStrategy.formatValue(fuzzValue) + "]. All other details are similar to a happy flow")
                .build());
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
        return "Iterate through each field name and insert zero-width characters";
    }
}
