package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.field.base.BaseReplaceFieldsPlaybook;
import dev.dochia.cli.core.util.JsonUtils;
import dev.dochia.cli.core.model.PlaybookData;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook that replaces primitive JSON fields with a dummy JSON array.
 */
@Singleton
@FieldPlaybook
public class ReplacePrimitivesWithArraysFieldsPlaybook extends BaseReplaceFieldsPlaybook {
    /**
     * Creates a new ReplacePrimitivesWithArraysFieldsPlaybook instance.
     *
     * @param ce the executor
     */
    public ReplacePrimitivesWithArraysFieldsPlaybook(FieldsIteratorExecutor ce) {
        super(ce);
    }

    @Override
    public BaseReplaceFieldsContext getContext(PlaybookData data) {
        return BaseReplaceFieldsContext.builder()
                .replaceWhat("primitive")
                .replaceWith("array")
                .skipMessage("Playbook only runs for primitives")
                .fieldFilter(field -> JsonUtils.isPrimitive(data.getPayload(), field))
                .fuzzValueProducer((schema, field) -> List.of("[{\"dochiaKey1\":\"dochiaValue1\"},{\"dochiaKey2\":\"dochiaValue2\"}]"))
                .build();
    }
}