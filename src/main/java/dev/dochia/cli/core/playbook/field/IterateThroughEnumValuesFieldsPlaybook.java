package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.field.base.BaseEnumIteratorFieldsPlaybook;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Fields that iterates through enum values and sends one request per each value.
 */
@Singleton
@FieldPlaybook
public class IterateThroughEnumValuesFieldsPlaybook extends BaseEnumIteratorFieldsPlaybook {

    /**
     * Creates a new IterateThroughEnumValuesFieldsPlaybook instance.
     *
     * @param ce the executor
     */
    public IterateThroughEnumValuesFieldsPlaybook(FieldsIteratorExecutor ce) {
        super(ce);
    }

    @Override
    protected BiFunction<Schema<?>, String, List<Object>> fuzzValueProducer() {
        return (schema, field) -> schema.getEnum()
                .stream()
                .map(Object.class::cast)
                .toList();
    }

    @Override
    protected String scenario() {
        return "Iterate through each possible enum values and send happy flow requests.";
    }


    @Override
    public String description() {
        return "Iterate through each enum field and send happy path requests with each possible enum value";
    }

}
