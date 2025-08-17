package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutorContext;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.util.JsonUtils;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Playbook that send default values for each field, if defined.
 */
@Singleton
@FieldPlaybook
public class DefaultValuesInFieldsPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(getClass());
    private final FieldsIteratorExecutor executor;

    /**
     * Creates a new DefaultValuesInFieldsPlaybook instance.
     *
     * @param ce the executor
     */
    public DefaultValuesInFieldsPlaybook(FieldsIteratorExecutor ce) {
        this.executor = ce;
    }

    @Override
    public void run(PlaybookData data) {
        Predicate<Schema<?>> isNotEnum = schema -> schema.getEnum() == null;
        Predicate<Schema<?>> hasDefault = schema -> schema.getDefault() != null;
        BiFunction<Schema<?>, String, List<Object>> fuzzedValueProducer = (schema, field) -> List.of(schema.getDefault());
        Predicate<String> isNotDiscriminator = executor::isFieldNotADiscriminator;
        Predicate<String> isFieldInRequestPayload = field -> JsonUtils.isFieldInJson(data.getPayload(), field);

        executor.execute(
                FieldsIteratorExecutorContext.builder()
                        .scenario("Iterate through each field with default value defined and send happy flow requests.")
                        .playbookData(data).fuzzingStrategy(FuzzingStrategy.replace())
                        .expectedResponseCode(ResponseCodeFamilyPredefined.TWOXX)
                        .schemaFilter(isNotEnum.and(hasDefault))
                        .fieldFilter(isNotDiscriminator.and(isFieldInRequestPayload))
                        .fuzzValueProducer(fuzzedValueProducer)
                        .skipMessage("It does not have a defined default value.")
                        .logger(logger)
                        .simpleReplaceField(true)
                        .testCasePlaybook(this)
                        .build());
    }

    @Override
    public String description() {
        return "Iterate through each field with default values and send a happy path request";
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }
}
