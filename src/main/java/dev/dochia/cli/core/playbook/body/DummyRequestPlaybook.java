package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that sends a dummy request.
 */
@Singleton
@BodyPlaybook
public class DummyRequestPlaybook extends BaseHttpWithPayloadSimplePlaybook {
    static final String DUMMY_JSON = "{\"dochia\":\"dochia\"}";

    /**
     * Creates a new DummyRequestPlaybook instance
     *
     * @param executor the executor
     */
    @Inject
    public DummyRequestPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getScenario() {
        return "Send a dummy JSON";
    }

    @Override
    protected String getPayload(PlaybookData data) {
        return DUMMY_JSON;
    }

    @Override
    public String description() {
        return "send a dummy json request {'dochia': 'dochia'}";
    }
}
