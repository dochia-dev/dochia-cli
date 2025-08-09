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
    private final ServiceCaller serviceCaller;
    private final TestCaseListener testCaseListener;

    /**
     * Creates a new InsertWhitespacesInFieldNamesFieldPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     */
    public InsertWhitespacesInFieldNamesFieldPlaybook(ServiceCaller sc, TestCaseListener lr) {
        this.serviceCaller = sc;
        this.testCaseListener = lr;
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
                testCaseListener.createAndExecuteTest(logger, this, () -> process(data, field, randomWhitespace), data);
            }
        }
    }

    private void process(PlaybookData data, String field, String randomWhitespace) {
        testCaseListener.addScenario(logger, "Insert random whitespaces in the field name [{}]", field);
        testCaseListener.addExpectedResult(logger, "Should get a [{}] response code", ResponseCodeFamilyPredefined.FOURXX);

        String fuzzedJson = JsonUtils.insertCharactersInFieldKey(data.getPayload(), field, randomWhitespace);

        HttpResponse response = serviceCaller.call(ServiceData.builder().relativePath(data.getPath()).headers(data.getHeaders())
                .payload(fuzzedJson).queryParams(data.getQueryParams()).httpMethod(data.getMethod()).contractPath(data.getContractPath())
                .contentType(data.getFirstRequestContentType()).pathParamsPayload(data.getPathParamsPayload())
                .build());
        testCaseListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.FOURXX);
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
        return "iterates through each request field name and insert random whitespaces";
    }
}