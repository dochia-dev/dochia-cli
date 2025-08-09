package dev.dochia.cli.core.playbook.field.base;

import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyDynamic;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutorContext;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.DochiaModelUtils;
import dev.dochia.cli.core.util.JsonUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Inject;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Base class for fuzzing fields by replacing them with values that are expect to return 2XX or 4XX response codes.
 */
public abstract class TwoXXorFourXXReplaceStringsPlaybook implements TestCasePlaybook {
    protected final PrettyLogger logger = PrettyLoggerFactory.getLogger(getClass());

    protected final TestCaseListener testCaseListener;
    protected final FieldsIteratorExecutor fieldsIteratorExecutor;

    /**
     * Constructor for initializing common dependencies for fuzzing fields.
     *
     * @param testCaseListener       the test case listener
     * @param fieldsIteratorExecutor the executor
     */
    @Inject
    protected TwoXXorFourXXReplaceStringsPlaybook(TestCaseListener testCaseListener, FieldsIteratorExecutor fieldsIteratorExecutor) {
        this.testCaseListener = testCaseListener;
        this.fieldsIteratorExecutor = fieldsIteratorExecutor;
    }

    @Override
    public void run(PlaybookData data) {
        ResponseCodeFamily expectedResponseCodes = new ResponseCodeFamilyDynamic(List.of("2XX", "4XX"));
        fieldsIteratorExecutor.execute(
                FieldsIteratorExecutorContext.builder()
                        .scenario("Replace fields with %s.".formatted(this.typesOfDataSentToTheService()))
                        .playbookData(data).fuzzingStrategy(FuzzingStrategy.replace())
                        .expectedResponseCode(expectedResponseCodes)
                        .fuzzValueProducer(this.fuzzValueProducer())
                        .fieldFilter(field -> JsonUtils.isFieldInJson(data.getPayload(), field))
                        .schemaFilter(DochiaModelUtils::isStringSchema)
                        .skipMessage("Field is not a string.")
                        .simpleReplaceField(true)
                        .logger(logger)
                        .testCasePlaybook(this)
                        .build());
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    /**
     * Override to provide the values used to fuzz.
     *
     * @return a BiFunction that produces the values used to fuzz
     */
    protected abstract BiFunction<Schema<?>, String, List<Object>> fuzzValueProducer();

    /**
     * Override to provide the types of data sent to the service.
     *
     * @return a String representing the types of data sent to the service
     */
    protected abstract String typesOfDataSentToTheService();
}
