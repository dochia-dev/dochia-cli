package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.field.base.BaseReplaceFieldsPlaybook;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.util.JsonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Playbook that overflows maps.
 */
@FieldPlaybook
@Singleton
public class OverflowMapSizeFieldsPlaybook extends BaseReplaceFieldsPlaybook {
    private final ProcessingArguments processingArguments;

    /**
     * Creates a new OverflowMapSizeFieldsPlaybook instance.
     *
     * @param ce the executor
     * @param pa to get the size of the overflow size
     */
    public OverflowMapSizeFieldsPlaybook(FieldsIteratorExecutor ce, ProcessingArguments pa) {
        super(ce);
        this.processingArguments = pa;
    }

    @Override
    public BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext getContext(PlaybookData data) {
        BiFunction<Schema<?>, String, List<Object>> fuzzValueProducer = (schema, field) -> {
            Object allMapKeys = JsonUtils.getVariableFromJson(data.getPayload(), field + ".keys()");
            String firstKey = allMapKeys instanceof String s ? s : ((Set<String>) allMapKeys).iterator().next();
            Object firstKeyValue = JsonUtils.getVariableFromJson(data.getPayload(), field + "." + firstKey);

            logger.debug("Fuzzing field {}", field);

            Map<String, Object> finalResult = new HashMap<>();
            int possibleMax = schema.getMaxProperties() != null ? schema.getMaxProperties() : processingArguments.getLargeStringsSize();
            int arraySize = Math.min(possibleMax, processingArguments.getLargeStringsSize() / 4) + 10;

            for (int i = 0; i < arraySize; i++) {
                finalResult.put(firstKey + i, firstKeyValue);
            }
            return List.of(JsonUtils.GSON.toJson(finalResult));
        };

        return BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext.builder()
                .replaceWhat("dictionary/hashmap")
                .replaceWith("overflow dictionary/hashmap")
                .skipMessage("Playbook only runs for dictionaries/hashmaps")
                .fieldFilter(field -> data.getRequestPropertyTypes().get(field).getAdditionalProperties() != null
                        && JsonUtils.isValidMap(data.getPayload(), field))
                .fuzzValueProducer(fuzzValueProducer)
                .build();
    }
}