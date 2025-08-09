package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.field.base.BaseReplaceFieldsPlaybook;
import dev.dochia.cli.core.util.JsonUtils;
import dev.dochia.cli.core.model.PlaybookData;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook that replaces JSON objects with primitive values.
 */
@FieldPlaybook
@Singleton
public class ReplaceObjectsWithPrimitivesFieldsPlaybook extends BaseReplaceFieldsPlaybook {
    /**
     * Creates a new ReplaceObjectsWithPrimitivesFieldsPlaybook instance.
     *
     * @param ce the executor
     */
    public ReplaceObjectsWithPrimitivesFieldsPlaybook(FieldsIteratorExecutor ce) {
        super(ce);
    }

    @Override
    public BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext getContext(PlaybookData data) {
        return BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext.builder()
                .replaceWhat("non-primitive")
                .replaceWith("primitive")
                .skipMessage("Playbook only runs for objects")
                .fieldFilter(field -> JsonUtils.isObject(data.getPayload(), field))
                .fuzzValueProducer((schema, field) -> List.of("dochia_primitive_string"))
                .build();
    }
}
