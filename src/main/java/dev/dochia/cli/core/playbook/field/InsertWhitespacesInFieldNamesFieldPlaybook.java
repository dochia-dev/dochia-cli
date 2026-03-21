package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
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

import java.util.List;
import java.util.Set;

@FieldPlaybook
@Singleton
public class InsertWhitespacesInFieldNamesFieldPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(InsertWhitespacesInFieldNamesFieldPlaybook.class);
    private final SimpleExecutor simpleExecutor;

    /**
     * Creates a new InsertWhitespacesInFieldNamesFieldPlaybook instance.
     *
     * @param simpleExecutor the simple executor
     */
    public InsertWhitespacesInFieldNamesFieldPlaybook(SimpleExecutor simpleExecutor) {
        this.simpleExecutor = simpleExecutor;
    }

    @Override
    public void run(PlaybookData data) {
        if (JsonUtils.isEmptyPayload(data.getPayload())) {
            logger.debug("Skipping playbook for path {} because the payload is empty", data.getPath());
            return;
        }

        Set<String> allFieldsByHttpMethod = data.getAllFieldsByHttpMethod();
        String randomWhitespace = UnicodeGenerator.generateRandomUnicodeString(2, Character::isWhitespace);

        for (String field : allFieldsByHttpMethod) {
            logger.debug("Fuzzing field {}, inserting {}", field, randomWhitespace);
            if (JsonUtils.isFieldInJson(data.getPayload(), field)) {
                process(data, field, randomWhitespace);
            }
        }
    }

    private void process(PlaybookData data, String field, String randomWhitespace) {
        String fuzzedJson = JsonUtils.insertCharactersInFieldKey(data.getPayload(), field, randomWhitespace);

        simpleExecutor.execute(SimpleExecutorContext.builder()
                .logger(logger)
                .testCasePlaybook(this)
                .playbookData(data)
                .payload(fuzzedJson)
                .expectedResponseCode(ResponseCodeFamilyPredefined.FOURXX)
                .scenario("Insert random whitespaces in the field name [" + field + "]")
                .build());
    }

    @Override
    public List<HttpMethod> skipForHttpMethods() {
        return List.of(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD);
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public String description() {
        return "Iterate through each request field name and insert random whitespaces";
    }
}