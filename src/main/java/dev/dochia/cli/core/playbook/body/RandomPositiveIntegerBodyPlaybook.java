package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.generator.simple.NumberGenerator;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that sends a random positive integer as body.
 */
@Singleton
@BodyPlaybook
public class RandomPositiveIntegerBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {
    /**
     * Creates a new RandomPositiveIntegerBodyPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public RandomPositiveIntegerBodyPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getPayload(PlaybookData data) {
        return NumberGenerator.generateVeryLargeInteger(3);
    }

    @Override
    protected String getScenario() {
        return "Send a request with a random positive integer body";
    }

    @Override
    public String description() {
        return "Send request with random positive integer body";
    }
}
