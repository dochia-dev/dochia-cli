package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that sends an empty body.
 */
@Singleton
@BodyPlaybook
public class EmptyBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {

    /**
     * Creates a new EmptyBodyPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public EmptyBodyPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getScenario() {
        return "Send a request with an empty string body";
    }

    @Override
    protected String getPayload(PlaybookData data) {
        return "";
    }

    @Override
    public String description() {
        return "send a request with a empty string body";
    }
}