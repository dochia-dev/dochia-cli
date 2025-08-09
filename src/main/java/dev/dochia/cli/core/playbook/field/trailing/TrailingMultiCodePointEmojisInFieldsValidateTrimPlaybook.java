package dev.dochia.cli.core.playbook.field.trailing;

import dev.dochia.cli.core.playbook.api.EmojiPlaybook;
import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.ValidateAndTrim;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.report.TestCaseListener;
import jakarta.inject.Singleton;

/**
 * Playbook that trails valid field values with multi code point emojis.
 */
@Singleton
@FieldPlaybook
@EmojiPlaybook
@ValidateAndTrim
public class TrailingMultiCodePointEmojisInFieldsValidateTrimPlaybook extends TrailingMultiCodePointEmojisInFieldsTrimValidatePlaybook {

    /**
     * Creates a new TrailingMultiCodePointEmojisInFieldsValidateTrimPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    protected TrailingMultiCodePointEmojisInFieldsValidateTrimPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern() {
        return ResponseCodeFamilyPredefined.FOURXX;
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenOptionalFieldsAreFuzzed() {
        return ResponseCodeFamilyPredefined.FOURXX;
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenRequiredFieldsAreFuzzed() {
        return ResponseCodeFamilyPredefined.FOURXX;
    }
}