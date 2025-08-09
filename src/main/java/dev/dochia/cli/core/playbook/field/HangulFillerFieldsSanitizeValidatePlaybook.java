package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.SanitizeAndValidate;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.field.base.ExpectOnly2XXBaseFieldsPlaybook;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Inserts Hangul Filler characters into field values.
 */
@FieldPlaybook
@Singleton
@SanitizeAndValidate
public class HangulFillerFieldsSanitizeValidatePlaybook extends ExpectOnly2XXBaseFieldsPlaybook {
    public HangulFillerFieldsSanitizeValidatePlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    protected String typeOfDataSentToTheService() {
        return "Hangul filler characters";
    }

    @Override
    protected List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
        List<String> fillers = List.of("\u3164", "\uFFA0", "\u115F", "\u1160");
        return FuzzingStrategy.getFuzzingStrategies(data, fuzzedField, fillers, true);
    }

    @Override
    protected boolean isPlaybookApplicable(PlaybookData data, String fuzzedField) {
        return super.isFieldSkippableForSpecialCharsPlaybooks(data, fuzzedField);
    }

    @Override
    public String description() {
        return "Inject Hangul filler characters to test for hidden-input handling";
    }
}
