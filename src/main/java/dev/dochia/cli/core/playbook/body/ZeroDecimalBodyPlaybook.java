package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that sends decimal zero 0.0 as body.
 */
@Singleton
@BodyPlaybook
public class ZeroDecimalBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {

    /**
     * Creates a new ZeroDecimalBodyPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public ZeroDecimalBodyPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getPayload(PlaybookData data) {
        return "0.0";
    }

    @Override
    protected String getScenario() {
        return "Send a request with decimal 0.0 as body";
    }

    @Override
    public String description() {
        return "Send request with decimal 0.0 as body";
    }
}
