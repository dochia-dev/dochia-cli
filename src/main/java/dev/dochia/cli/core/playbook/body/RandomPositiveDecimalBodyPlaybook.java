package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.generator.simple.NumberGenerator;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that sends a positive decimal body.
 */
@Singleton
@BodyPlaybook
public class RandomPositiveDecimalBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {
    /**
     * Creates a new RandomPositiveDecimalBodyPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public RandomPositiveDecimalBodyPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getPayload(PlaybookData data) {
        return NumberGenerator.generateVeryLargeDecimal(3);
    }

    @Override
    protected String getScenario() {
        return "Send a request with an random positive decimal body";
    }

    @Override
    public String description() {
        return "Send request with random positive decimal body";
    }
}
