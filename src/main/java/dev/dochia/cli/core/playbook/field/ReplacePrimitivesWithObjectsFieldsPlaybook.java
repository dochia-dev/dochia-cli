package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.field.base.BaseReplaceFieldsPlaybook;
import dev.dochia.cli.core.util.JsonUtils;
import dev.dochia.cli.core.model.PlaybookData;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook that replaces JSON primitive fields with a dummy JSON object.
 */
@FieldPlaybook
@Singleton
public class ReplacePrimitivesWithObjectsFieldsPlaybook extends BaseReplaceFieldsPlaybook {
    /**
     * Creates a new ReplacePrimitivesWithObjectsFieldsPlaybook instance.
     *
     * @param ce the executor
     */
    public ReplacePrimitivesWithObjectsFieldsPlaybook(FieldsIteratorExecutor ce) {
        super(ce);
    }

    @Override
    public BaseReplaceFieldsContext getContext(PlaybookData data) {
        return BaseReplaceFieldsContext.builder()
                .replaceWhat("primitive")
                .replaceWith("object")
                .skipMessage("Playbook only runs for primitives")
                .fieldFilter(field -> JsonUtils.isPrimitive(data.getPayload(), field))
                .fuzzValueProducer((schema, field) -> List.of("{\"dochiaKey1\":\"dochiaValue1\",\"dochiaKey2\":20}"))
                .build();
    }
}