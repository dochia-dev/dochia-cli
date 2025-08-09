package dev.dochia.cli.core.playbook.field.only;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.report.TestCaseListener;

/** Base class for playbooks sending only invisible chars in fields. */
public abstract class InvisibleCharsOnlyValidateTrimPlaybook
    extends InvisibleCharsOnlyTrimValidatePlaybook {

  /**
   * Constructor for initializing common dependencies for fuzzing fields with invisible chars.
   *
   * @param sc the service caller
   * @param lr the test case listener
   * @param cp files arguments
   * @param fa filter arguments
   */
  protected InvisibleCharsOnlyValidateTrimPlaybook(
      ServiceCaller sc, TestCaseListener lr, FilesArguments cp, FilterArguments fa) {
    super(sc, lr, cp, fa);
  }

  @Override
  public ResponseCodeFamily getExpectedHttpCodeWhenOptionalFieldsAreFuzzed() {
    return ResponseCodeFamilyPredefined.FOURXX;
  }
}
