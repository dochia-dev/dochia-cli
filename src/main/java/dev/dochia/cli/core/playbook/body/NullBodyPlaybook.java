package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that will send a null request.
 */
@Singleton
@BodyPlaybook
public class NullBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {

    /**
     * Creates a new NullBodyPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public NullBodyPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getPayload(PlaybookData data) {
        return "null";
    }

    @Override
    protected String getScenario() {
        return "Send a request with a NULL body";
    }

    @Override
    public String description() {
        return "Send request with null body";
    }
}