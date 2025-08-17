package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that sends a malformed JSON request.
 */
@Singleton
@BodyPlaybook
public class MalformedJsonPlaybook extends BaseHttpWithPayloadSimplePlaybook {

    /**
     * Creates a new MalformedJsonPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public MalformedJsonPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getScenario() {
        return "Send a malformed JSON which has the string 'bla' at the end";
    }

    @Override
    protected String getPayload(PlaybookData data) {
        return data.getPayload() + "bla";
    }

    @Override
    public String description() {
        return "Send malformed JSON request with trailing 'bla' string";
    }
}
