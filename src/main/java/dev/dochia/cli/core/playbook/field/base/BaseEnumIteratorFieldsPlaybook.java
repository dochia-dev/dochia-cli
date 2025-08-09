package dev.dochia.cli.core.playbook.field.base;

import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutorContext;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.JsonUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Base class for playbooks that iterate through enum values in fields.
 * It provides a common structure for fuzzing enum fields in API requests.
 */
public abstract class BaseEnumIteratorFieldsPlaybook implements TestCasePlaybook {
    protected final PrettyLogger logger = PrettyLoggerFactory.getLogger(getClass());
    protected final FieldsIteratorExecutor executor;

    /**
     * Constructor for initializing common dependencies for fuzzing enum fields.
     *
     * @param executor the executor used to perform the fuzzing
     */
    protected BaseEnumIteratorFieldsPlaybook(FieldsIteratorExecutor executor) {
        this.executor = executor;
    }

    /**
     * Produces fuzz values for the given schema and field.
     *
     * @return a BiFunction that takes a Schema and a field name, and returns a list of fuzz values
     */
    protected abstract BiFunction<Schema<?>, String, List<Object>> fuzzValueProducer();

    /**
     * The description of the scenario being executed.
     *
     * @return a string describing the scenario
     */
    protected abstract String scenario();

    @Override
    public void run(PlaybookData data) {
        Predicate<Schema<?>> schemaFilter = schema -> schema.getEnum() != null;
        Predicate<String> notADiscriminator = executor::isFieldNotADiscriminator;
        Predicate<String> fieldExists = field -> JsonUtils.isFieldInJson(data.getPayload(), field);

        executor.execute(
                FieldsIteratorExecutorContext.builder()
                        .scenario(this.scenario())
                        .playbookData(data)
                        .fuzzingStrategy(FuzzingStrategy.replace())
                        .expectedResponseCode(ResponseCodeFamilyPredefined.TWOXX)
                        .skipMessage("It's either not an enum or it's a discriminator.")
                        .fieldFilter(notADiscriminator.and(fieldExists))
                        .schemaFilter(schemaFilter)
                        .fuzzValueProducer(this.fuzzValueProducer())
                        .simpleReplaceField(true)
                        .logger(logger)
                        .testCasePlaybook(this)
                        .build());
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }
}
