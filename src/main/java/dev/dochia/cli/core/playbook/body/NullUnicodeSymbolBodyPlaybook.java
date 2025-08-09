package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that sends the null unicode symbol as body.
 */
@Singleton
@BodyPlaybook
public class NullUnicodeSymbolBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {

    /**
     * Creates a new NullUnicodeSymbolBodyPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public NullUnicodeSymbolBodyPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getPayload(PlaybookData data) {
        return "␀";
    }

    @Override
    protected String getScenario() {
        return "Send a request with a ␀ body";
    }

    @Override
    public String description() {
        return "send a request with a ␀ body";
    }
}