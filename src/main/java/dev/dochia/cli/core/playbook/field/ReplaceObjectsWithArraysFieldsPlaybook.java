package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.field.base.BaseReplaceFieldsPlaybook;
import dev.dochia.cli.core.util.JsonUtils;
import dev.dochia.cli.core.model.PlaybookData;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook that replaces JSON objects with arrays.
 */
@FieldPlaybook
@Singleton
public class ReplaceObjectsWithArraysFieldsPlaybook extends BaseReplaceFieldsPlaybook {

    /**
     * Creates a new ReplaceObjectsWithArraysFieldsPlaybook instance.
     *
     * @param ce the executor
     */
    public ReplaceObjectsWithArraysFieldsPlaybook(FieldsIteratorExecutor ce) {
        super(ce);
    }

    @Override
    public BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext getContext(PlaybookData data) {
        return BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext.builder()
                .replaceWhat("object")
                .replaceWith("array")
                .skipMessage("Playbook only runs for objects")
                .fieldFilter(field -> JsonUtils.isObject(data.getPayload(), field) && !JsonUtils.isArray(data.getPayload(), field))
                .fuzzValueProducer((schema, field) -> List.of("[{\"dochiaKey1\":\"dochiaValue1\",\"dochiaKey2\":20},{\"dochiaKey3\":\"dochiaValue3\",\"dochiaKey3\":40}]"))
                .build();
    }
}
