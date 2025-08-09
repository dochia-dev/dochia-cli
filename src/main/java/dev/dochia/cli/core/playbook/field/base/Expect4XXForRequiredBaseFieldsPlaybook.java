package dev.dochia.cli.core.playbook.field.base;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.report.TestCaseListener;

/**
 * Abstract base class for playbooks expecting 4xx responses for required base fields. Extends the
 * {@link BaseFieldsPlaybook} class and provides a constructor to initialize common dependencies for
 * fuzzing required base fields with the expectation of 4xx responses.
 */
public abstract class Expect4XXForRequiredBaseFieldsPlaybook extends BaseFieldsPlaybook {

  /**
   * Constructor for initializing common dependencies for fuzzing required base fields with the
   * expectation of 4xx responses.
   *
   * @param sc The {@link ServiceCaller} used to make service calls.
   * @param lr The {@link TestCaseListener} for reporting test case events.
   * @param cp The {@link FilesArguments} for file-related arguments.
   */
  protected Expect4XXForRequiredBaseFieldsPlaybook(
      ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
    super(sc, lr, cp);
  }

  @Override
  public ResponseCodeFamily getExpectedHttpCodeWhenRequiredFieldsAreFuzzed() {
    return ResponseCodeFamilyPredefined.FOURXX;
  }

  @Override
  public ResponseCodeFamily getExpectedHttpCodeWhenOptionalFieldsAreFuzzed() {
    return ResponseCodeFamilyPredefined.TWOXX;
  }
}
