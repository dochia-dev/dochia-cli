package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.field.base.BaseReplaceFieldsPlaybook;
import dev.dochia.cli.core.util.JsonUtils;
import dev.dochia.cli.core.model.PlaybookData;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook that replaces JSON arrays with primitive values.
 */
@Singleton
@FieldPlaybook
public class ReplaceArraysWithPrimitivesFieldsPlaybook extends BaseReplaceFieldsPlaybook {
    /**
     * Creates a new ReplaceArraysWithPrimitivesFieldsPlaybook instance.
     *
     * @param ce the executor
     */
    public ReplaceArraysWithPrimitivesFieldsPlaybook(FieldsIteratorExecutor ce) {
        super(ce);
    }

    @Override
    public BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext getContext(PlaybookData data) {
        return BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext.builder()
                .replaceWhat("array")
                .replaceWith("primitive")
                .skipMessage("Playbook only runs for arrays")
                .fieldFilter(field -> JsonUtils.isArray(data.getPayload(), field))
                .fuzzValueProducer((schema, field) -> List.of("dochia_primitive_string"))
                .build();
    }
}
