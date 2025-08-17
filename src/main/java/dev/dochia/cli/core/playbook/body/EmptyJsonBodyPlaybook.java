package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that send am empty json body.
 */
@Singleton
@BodyPlaybook
public class EmptyJsonBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {

    /**
     * Creates a new EmptyJsonBodyPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public EmptyJsonBodyPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getScenario() {
        return "Send a request with an empty json body";
    }

    @Override
    protected String getPayload(PlaybookData data) {
        return "{}";
    }

    @Override
    public String description() {
        return "Send request with empty JSON object body";
    }
}