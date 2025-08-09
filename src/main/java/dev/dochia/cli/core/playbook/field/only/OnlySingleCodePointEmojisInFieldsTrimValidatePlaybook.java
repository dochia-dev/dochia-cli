package dev.dochia.cli.core.playbook.field.only;

import dev.dochia.cli.core.playbook.api.EmojiPlaybook;
import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.TrimAndValidate;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.report.TestCaseListener;
import jakarta.inject.Singleton;

import java.util.List;

/** Playbook that sends only single code point emojis in fields. */
@Singleton
@FieldPlaybook
@EmojiPlaybook
@TrimAndValidate
public class OnlySingleCodePointEmojisInFieldsTrimValidatePlaybook
    extends InvisibleCharsOnlyTrimValidatePlaybook {

  /**
   * Creates a new OnlySingleCodePointEmojisInFieldsTrimValidatePlaybook instance.
   *
   * @param sc the service caller
   * @param lr the test case listener
   * @param cp files arguments
   * @param fa filter arguments
   */
  public OnlySingleCodePointEmojisInFieldsTrimValidatePlaybook(
      ServiceCaller sc, TestCaseListener lr, FilesArguments cp, FilterArguments fa) {
    super(sc, lr, cp, fa);
  }

  @Override
  protected String typeOfDataSentToTheService() {
    return "values with single code point emojis only";
  }

  @Override
  List<String> getInvisibleChars() {
    return UnicodeGenerator.getSingleCodePointEmojis();
  }
}
