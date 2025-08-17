package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.generator.simple.NumberGenerator;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that sends a negative decimal number as body.
 */
@Singleton
@BodyPlaybook
public class RandomNegativeDecimalBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {
    /**
     * Creates a new RandomNegativeDecimalBodyPlaybook instance.
     *
     * @param executor the executor
     */
    @Inject
    public RandomNegativeDecimalBodyPlaybook(SimpleExecutor executor) {
        super(executor);
    }

    @Override
    protected String getPayload(PlaybookData data) {
        double value = Double.parseDouble(NumberGenerator.generateVeryLargeDecimal(3));
        return String.valueOf(-value);
    }

    @Override
    protected String getScenario() {
        return "Send a request with an random negative decimal body";
    }

    @Override
    public String description() {
        return "Send request with random negative decimal body";
    }
}
