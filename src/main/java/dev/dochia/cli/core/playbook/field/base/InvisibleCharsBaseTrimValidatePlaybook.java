package dev.dochia.cli.core.playbook.field.base;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import java.util.List;

/**
 * Abstract base class for playbooks targeting invisible characters in base fields with trim and validation.
 * Extends the {@link ExpectOnly2XXBaseFieldsPlaybook} class and provides a constructor
 * to initialize common dependencies for fuzzing base fields with the expectation of only 2xx responses,
 * along with handling invisible characters, trimming, and validation.
 */
public abstract class InvisibleCharsBaseTrimValidatePlaybook extends ExpectOnly2XXBaseFieldsPlaybook {

    /**
     * Constructor for initializing common dependencies for fuzzing base fields with the expectation of only 2xx responses,
     * along with handling invisible characters, trimming, and validation.
     *
     * @param sc The {@link ServiceCaller} used to make service calls.
     * @param lr The {@link TestCaseListener} for reporting test case events.
     * @param cp The {@link FilesArguments} for file-related arguments.
     */
    protected InvisibleCharsBaseTrimValidatePlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    public List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
        return this.getInvisibleChars()
                .stream().map(value -> concreteFuzzStrategy().withData(value)).toList();
    }

    @Override
    public boolean isPlaybookApplicable(PlaybookData data, String fuzzedField) {
        return testCaseListener.isFieldNotADiscriminator(fuzzedField);
    }

    @Override
    public String description() {
        return "iterate through each field and send " + this.typeOfDataSentToTheService();
    }

    /**
     * Override to provide the list of invisible chars used for fuzzing.
     *
     * @return the list with invisible chars used for fuzzing
     */
    public abstract List<String> getInvisibleChars();

    /**
     * What is the actual fuzzing strategy to apply.
     *
     * @return the concrete fuzzing strategy to apply
     */
    public abstract FuzzingStrategy concreteFuzzStrategy();
}
