package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.JsonUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.List;

/**
 * Playbook that inserts random values in request bodies.
 */
@BodyPlaybook
@Singleton
public class InsertRandomValuesInBodyPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(getClass());
    private final SimpleExecutor simpleExecutor;

    /**
     * Creates a new InsertRandomValuesInBodyPlaybook instance.
     *
     * @param ce the executor
     */
    @Inject
    InsertRandomValuesInBodyPlaybook(SimpleExecutor ce) {
        this.simpleExecutor = ce;
    }

    @Override
    public void run(PlaybookData data) {
        if (!JsonUtils.isEmptyPayload(data.getPayload())) {
            for (String maliciousPayload : UnicodeGenerator.getInvalidJsons()) {
                char firstChar = data.getPayload().charAt(0);
                String finalPayload = firstChar + maliciousPayload + "," + data.getPayload().substring(1);

                simpleExecutor.execute(
                        SimpleExecutorContext.builder()
                                .expectedResponseCode(ResponseCodeFamilyPredefined.FOURXX)
                                .playbookData(data)
                                .logger(logger)
                                .replaceRefData(false)
                                .scenario("Insert invalid data %s within a valid json request body".formatted(maliciousPayload))
                                .testCasePlaybook(this)
                                .payload(finalPayload)
                                .build());
            }
        }
    }

    @Override
    public String description() {
        return "insert invalid data within a valid request body";
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public List<HttpMethod> skipForHttpMethods() {
        return Arrays.asList(HttpMethod.GET, HttpMethod.DELETE);
    }
}
