package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that sends random unicode characters as body.
 */
@Singleton
@BodyPlaybook
public class RandomUnicodeBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {
    /**
     * Creates a new RandomUnicodeBodyPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public RandomUnicodeBodyPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getPayload(PlaybookData data) {
        return String.join("", UnicodeGenerator.getControlCharsFields());
    }

    @Override
    protected String getScenario() {
        return "Send a request with an random unicode string body";
    }

    @Override
    public String description() {
        return "Send request with random unicode string body";
    }
}
