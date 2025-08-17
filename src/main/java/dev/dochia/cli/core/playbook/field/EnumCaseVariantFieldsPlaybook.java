package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.field.base.BaseEnumIteratorFieldsPlaybook;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

@Singleton
@FieldPlaybook
public class EnumCaseVariantFieldsPlaybook extends BaseEnumIteratorFieldsPlaybook {

    /**
     * Constructor for initializing the playbook with the provided executor.
     *
     * @param executor the executor used to perform the fuzzing
     */
    public EnumCaseVariantFieldsPlaybook(FieldsIteratorExecutor executor) {
        super(executor);
    }

    @Override
    protected BiFunction<Schema<?>, String, List<Object>> fuzzValueProducer() {
        return (schema, field) -> schema.getEnum().stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(CommonUtils::randomizeCase)
                .filter(fuzzed -> !schema.getEnum().contains(fuzzed))
                .map(Object.class::cast)
                .toList();
    }

    @Override
    protected String scenario() {
        return "Iterate through each possible enum values and send random casing.";
    }

    @Override
    public String description() {
        return "Iterate through each enum field and send case-variant values to test case sensitivity";
    }
}