package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.field.base.BaseReplaceFieldsPlaybook;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.JsonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Playbook that overflows arrays.
 */
@Singleton
@FieldPlaybook
public class OverflowArraySizeFieldsPlaybook extends BaseReplaceFieldsPlaybook {
    private final ProcessingArguments processingArguments;

    /**
     * Creates a new OverflowArraySizeFieldsPlaybook instance.
     *
     * @param ce the executor
     * @param pa to get the size of the overflow size
     */
    public OverflowArraySizeFieldsPlaybook(FieldsIteratorExecutor ce, ProcessingArguments pa) {
        super(ce);
        this.processingArguments = pa;
    }

    @Override
    public BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext getContext(PlaybookData data) {
        BiFunction<Schema<?>, String, List<Object>> fuzzValueProducer = (schema, string) -> {
            int size = schema.getMaxItems() != null ? schema.getMaxItems() : processingArguments.getLargeStringsSize();
            logger.info("Fuzzing field [{}] with an array of size [{}]", string, size);

            String fieldValue = JsonUtils.serialize(JsonUtils.getVariableFromJson(data.getPayload(), string + "[0]"));
            int repetitions = CommonUtils.getMaxArraySizeBasedOnFieldsLength(Optional.ofNullable(fieldValue).orElse("ARRAY"), size);

            return List.of("[" + StringUtils.repeat(fieldValue, ",", repetitions) + "]");
        };

        return BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext.builder()
                .replaceWhat("array")
                .replaceWith("overflow array")
                .skipMessage("Playbook only runs for arrays")
                .fieldFilter(field -> JsonUtils.isArray(data.getPayload(), field))
                .fuzzValueProducer(fuzzValueProducer)
                .build();
    }
}