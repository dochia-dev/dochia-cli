package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.SanitizeAndValidate;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.field.base.ExpectOnly2XXBaseFieldsPlaybook;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook that sends abugidas characters in string fields for services following the sanitize then validate strategy.
 */
@Singleton
@FieldPlaybook
@SanitizeAndValidate
public class AbugidasInStringFieldsSanitizeValidatePlaybook extends ExpectOnly2XXBaseFieldsPlaybook {

    /**
     * Creates a new AbugidasInStringFieldsSanitizeValidatePlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    protected AbugidasInStringFieldsSanitizeValidatePlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    public List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
        return FuzzingStrategy.getFuzzingStrategies(data, fuzzedField, UnicodeGenerator.getAbugidasChars(), false);
    }

    @Override
    protected String typeOfDataSentToTheService() {
        return "values containing abugidas chars";
    }

    @Override
    public boolean isPlaybookApplicable(PlaybookData data, String fuzzedField) {
        return super.isFieldSkippableForSpecialCharsPlaybooks(data, fuzzedField);
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern() {
        return ResponseCodeFamilyPredefined.FOURXX_TWOXX;
    }

    @Override
    public String description() {
        return "iterate through each field and send " + typeOfDataSentToTheService();
    }
}
