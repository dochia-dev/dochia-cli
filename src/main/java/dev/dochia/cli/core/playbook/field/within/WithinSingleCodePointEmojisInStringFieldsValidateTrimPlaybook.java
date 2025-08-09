package dev.dochia.cli.core.playbook.field.within;

import dev.dochia.cli.core.playbook.api.EmojiPlaybook;
import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.ValidateAndSanitize;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.field.base.InvisibleCharsBaseTrimValidatePlaybook;
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
 * Playbook that inserts single code point emojis in valid field values.
 */
@Singleton
@FieldPlaybook
@EmojiPlaybook
@ValidateAndSanitize
public class WithinSingleCodePointEmojisInStringFieldsValidateTrimPlaybook extends InvisibleCharsBaseTrimValidatePlaybook {

    /**
     * Creates a new WithinSingleCodePointEmojisInStringFieldsValidateTrimPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    protected WithinSingleCodePointEmojisInStringFieldsValidateTrimPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern() {
        return ResponseCodeFamilyPredefined.FOURXX;
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenRequiredFieldsAreFuzzed() {
        return ResponseCodeFamilyPredefined.FOURXX;
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenOptionalFieldsAreFuzzed() {
        return ResponseCodeFamilyPredefined.FOURXX;
    }


    @Override
    public List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
        return FuzzingStrategy.getFuzzingStrategies(data, fuzzedField, this.getInvisibleChars(), true);
    }

    @Override
    protected String typeOfDataSentToTheService() {
        return "values containing single code point emojis";
    }

    @Override
    public List<String> getInvisibleChars() {
        return UnicodeGenerator.getSingleCodePointEmojis();
    }

    @Override
    public FuzzingStrategy concreteFuzzStrategy() {
        return FuzzingStrategy.replace();
    }
}