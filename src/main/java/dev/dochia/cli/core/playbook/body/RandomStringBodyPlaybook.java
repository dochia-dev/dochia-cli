package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.generator.simple.StringGenerator;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that sends a random string as a body.
 */
@Singleton
@BodyPlaybook
public class RandomStringBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {
    /**
     * Creates a RandomStringBodyPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public RandomStringBodyPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getPayload(PlaybookData data) {
        return StringGenerator.generateRandomString();
    }

    @Override
    protected String getScenario() {
        return "Send a request with an random string body";
    }

    @Override
    public String description() {
        return "send a request with a random string body";
    }
}
