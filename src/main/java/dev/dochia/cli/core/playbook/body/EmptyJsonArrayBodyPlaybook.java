package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that sends an empty json array body.
 */
@Singleton
@BodyPlaybook
public class EmptyJsonArrayBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {

    /**
     * Creates a new EmptyJsonArrayBodyPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public EmptyJsonArrayBodyPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getScenario() {
        return "Send a request with an empty json array body";
    }

    @Override
    protected String getPayload(PlaybookData data) {
        return "[]";
    }

    @Override
    public String description() {
        return "Send request with empty JSON array body";
    }
}