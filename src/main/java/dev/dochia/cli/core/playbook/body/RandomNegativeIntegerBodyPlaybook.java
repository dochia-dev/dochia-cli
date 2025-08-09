package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.generator.simple.NumberGenerator;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook used to send negative integers as body.
 */
@Singleton
@BodyPlaybook
public class RandomNegativeIntegerBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {
    /**
     * Creates a new RandomNegativeIntegerBodyPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public RandomNegativeIntegerBodyPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getPayload(PlaybookData data) {
        long value = Long.parseLong(NumberGenerator.generateVeryLargeInteger(3));
        return String.valueOf(-value);
    }

    @Override
    protected String getScenario() {
        return "Send a request with an random negative integer body";
    }

    @Override
    public String description() {
        return "send a request with a random negative integer body";
    }
}
