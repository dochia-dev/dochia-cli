package dev.dochia.cli.core.playbook.field.leading;

import dev.dochia.cli.core.playbook.api.EmojiPlaybook;
import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.TrimAndValidate;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.field.base.InvisibleCharsBaseTrimValidatePlaybook;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook prefixing valid field values with multi code point emojis.
 */
@Singleton
@FieldPlaybook
@EmojiPlaybook
@TrimAndValidate
public class LeadingMultiCodePointEmojisInFieldsTrimValidatePlaybook extends InvisibleCharsBaseTrimValidatePlaybook {

    /**
     * Creates a new LeadingMultiCodePointEmojisInFieldsTrimValidatePlaybook instance.
     *
     * @param sc The {@link ServiceCaller} used to make service calls
     * @param lr The {@link TestCaseListener} for reporting test case events
     * @param cp The {@link FilesArguments} for file-related arguments
     */
    protected LeadingMultiCodePointEmojisInFieldsTrimValidatePlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    protected String typeOfDataSentToTheService() {
        return "values prefixed with multi code points emojis";
    }

    @Override
    public List<String> getInvisibleChars() {
        return UnicodeGenerator.getMultiCodePointEmojis();
    }

    @Override
    public FuzzingStrategy concreteFuzzStrategy() {
        return FuzzingStrategy.prefix();
    }
}