package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.field.base.BaseReplaceFieldsPlaybook;
import dev.dochia.cli.core.util.JsonUtils;
import dev.dochia.cli.core.model.PlaybookData;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook that replace JSON arrays with simple objects.
 */
@FieldPlaybook
@Singleton
public class ReplaceArraysWithSimpleObjectsFieldsPlaybook extends BaseReplaceFieldsPlaybook {

    /**
     * Creates a new ReplaceArraysWithSimpleObjectsFieldsPlaybook instance.
     *
     * @param ce the executor
     */
    public ReplaceArraysWithSimpleObjectsFieldsPlaybook(FieldsIteratorExecutor ce) {
        super(ce);
    }

    @Override
    public BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext getContext(PlaybookData data) {
        return BaseReplaceFieldsPlaybook.BaseReplaceFieldsContext.builder()
                .replaceWhat("array")
                .replaceWith("simple object")
                .skipMessage("Playbook only runs for arrays")
                .fieldFilter(field -> JsonUtils.isArray(data.getPayload(), field))
                .fuzzValueProducer((schema, field) -> List.of("{\"dochiaKey1\":\"dochiaValue1\",\"dochiaKey2\":20}"))
                .build();
    }
}
