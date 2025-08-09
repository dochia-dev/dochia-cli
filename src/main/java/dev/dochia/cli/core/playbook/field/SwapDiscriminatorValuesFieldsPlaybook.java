package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.context.GlobalContext;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.field.base.BaseReplaceFieldsPlaybook;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.util.JsonUtils;
import jakarta.inject.Singleton;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Playbook that swaps discriminator values in fields.
 * It replaces the current discriminator value with another value from the set of available discriminator values.
 */
@Singleton
@FieldPlaybook
public class SwapDiscriminatorValuesFieldsPlaybook extends BaseReplaceFieldsPlaybook {
    private final GlobalContext globalContext;

    /**
     * Constructor for initializing common dependencies for fuzzing fields.
     *
     * @param ce the executor
     */
    protected SwapDiscriminatorValuesFieldsPlaybook(FieldsIteratorExecutor ce, GlobalContext globalContext) {
        super(ce);
        this.globalContext = globalContext;
    }

    @Override
    public BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext getContext(PlaybookData data) {
        return BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext.builder()
                .replaceWhat("discriminator")
                .replaceWith("swapped values")
                .skipMessage("Playbook only runs for discriminator fields")
                .fieldFilter(globalContext::isDiscriminator)
                .fuzzValueProducer((schema, field) -> {
                    String oldValue = String.valueOf(JsonUtils.getVariableFromJson(data.getPayload(), field));
                    return globalContext.getDiscriminatorValues().getOrDefault(field, Set.of()).stream()
                            .filter(Predicate.not(oldValue::equals))
                            .toList();
                })
                .build();
    }
}