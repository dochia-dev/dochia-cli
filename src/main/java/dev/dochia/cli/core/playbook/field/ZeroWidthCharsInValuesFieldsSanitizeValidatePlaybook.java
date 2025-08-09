package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.SanitizeAndValidate;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.field.base.ExpectOnly2XXBaseFieldsPlaybook;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook that sends zero-width characters in fields for the sanitize then validate strategy.
 */
@FieldPlaybook
@Singleton
@SanitizeAndValidate
public class ZeroWidthCharsInValuesFieldsSanitizeValidatePlaybook extends ExpectOnly2XXBaseFieldsPlaybook {
    /**
     * Constructor for initializing common dependencies for fuzzing base fields with the expectation of only 4xx responses.
     *
     * @param sc The {@link ServiceCaller} used to make service calls.
     * @param lr The {@link TestCaseListener} for reporting test case events.
     * @param cp The {@link FilesArguments} for file-related arguments.
     */
    protected ZeroWidthCharsInValuesFieldsSanitizeValidatePlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    protected String typeOfDataSentToTheService() {
        return "zero-width characters";
    }

    @Override
    protected List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
        return UnicodeGenerator.getZwCharsSmallListFields().stream()
                .map(fuzzVal -> FuzzingStrategy.insert().withData(fuzzVal)).toList();
    }

    @Override
    protected boolean shouldCheckForFuzzedValueMatchingPattern() {
        return false;
    }

    @Override
    public boolean isPlaybookApplicable(PlaybookData data, String fuzzedField) {
        return testCaseListener.isFieldNotADiscriminator(fuzzedField);
    }

    @Override
    public String description() {
        return "iterate through each field and send values containing zero-width characters";
    }
}
