package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.JsonUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Playbook that sends examples from components.examples; from schema.example(s), MediaType.example(s) Example can be a ref.
 */
@FieldPlaybook
@Singleton
public class ExamplesFieldsPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(ExamplesFieldsPlaybook.class);
    private final SimpleExecutor simpleExecutor;

    /**
     * Creates a new ExamplesFieldsPlaybook instance.
     *
     * @param simpleExecutor the executor
     */
    @Inject
    public ExamplesFieldsPlaybook(SimpleExecutor simpleExecutor) {
        this.simpleExecutor = simpleExecutor;
    }

    @Override
    public void run(PlaybookData data) {
        Set<String> payloads = new HashSet<>();
        payloads.add(Optional.ofNullable(data.getReqSchema().getExample())
                .map(JsonUtils::serialize)
                .orElse(""));

        payloads.addAll(Optional.ofNullable(data.getReqSchema().getExamples())
                .orElse(Collections.emptyList())
                .stream()
                .map(JsonUtils::serialize)
                .toList());

        payloads.addAll(data.getExamples()
                .stream()
                .map(JsonUtils::serialize)
                .toList());
        payloads.remove("");

        logger.debug("Fuzzing the following examples: {}", payloads);

        for (String payload : payloads) {
            simpleExecutor.execute(SimpleExecutorContext
                    .builder()
                    .payload(payload)
                    .logger(logger)
                    .testCasePlaybook(this)
                    .playbookData(data)
                    .scenario("Send a request for every unique example")
                    .expectedResponseCode(ResponseCodeFamilyPredefined.TWOXX)
                    .expectedSpecificResponseCode("2XX")
                    .build()
            );
        }
    }

    @Override
    public String description() {
        return "Send a request for every unique example defined in the contract";
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }
}
