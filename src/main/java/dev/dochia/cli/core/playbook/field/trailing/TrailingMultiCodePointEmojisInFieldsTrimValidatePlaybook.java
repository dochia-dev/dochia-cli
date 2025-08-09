package dev.dochia.cli.core.playbook.field.trailing;

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
 * Playbook that trails valid field values with multi code point emojis.
 */
@Singleton
@FieldPlaybook
@EmojiPlaybook
@TrimAndValidate
public class TrailingMultiCodePointEmojisInFieldsTrimValidatePlaybook extends InvisibleCharsBaseTrimValidatePlaybook {

    /**
     * Creates a new TrailingMultiCodePointEmojisInFieldsTrimValidatePlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    protected TrailingMultiCodePointEmojisInFieldsTrimValidatePlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    protected String typeOfDataSentToTheService() {
        return "values trailed with multi code point emojis";
    }

    @Override
    public List<String> getInvisibleChars() {
        return UnicodeGenerator.getMultiCodePointEmojis();
    }

    @Override
    public FuzzingStrategy concreteFuzzStrategy() {
        return FuzzingStrategy.trail();
    }
}