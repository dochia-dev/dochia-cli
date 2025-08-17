package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that sends the null unicode value as body.
 */
@Singleton
@BodyPlaybook
public class NullUnicodeBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {

    /**
     * Creates a new NullUnicodeBodyPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public NullUnicodeBodyPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getPayload(PlaybookData data) {
        return "\u0000";
    }

    @Override
    protected String getScenario() {
        return "Send a request with a \\u0000 body";
    }

    @Override
    public String description() {
        return "Send request with \\u0000 body";
    }
}