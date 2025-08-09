package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.field.base.ExpectOnly2XXBaseFieldsPlaybook;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Inserts fullwidth angle brackets to bypass simplistic filter checks.
 */
@FieldPlaybook
@Singleton
class FullwidthBracketsFieldsPlaybook extends ExpectOnly2XXBaseFieldsPlaybook {
    public FullwidthBracketsFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    protected String typeOfDataSentToTheService() {
        return "fullwidth angle bracket characters";
    }

    @Override
    public boolean isPlaybookApplicable(PlaybookData data, String fuzzedField) {
        return super.isFieldSkippableForSpecialCharsPlaybooks(data, fuzzedField);
    }

    @Override
    protected List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
        List<String> brackets = List.of("\uFF1C", "\uFF1E");
        return FuzzingStrategy.getFuzzingStrategies(data, fuzzedField, brackets, true);

    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern() {
        return ResponseCodeFamilyPredefined.FOURXX;
    }

    @Override
    public String description() {
        return "insert fullwidth '<' and '>' to test for markup filter bypass";
    }
}
