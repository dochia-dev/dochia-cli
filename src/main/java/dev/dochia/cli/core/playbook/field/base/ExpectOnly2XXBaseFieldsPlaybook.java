package dev.dochia.cli.core.playbook.field.base;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.report.TestCaseListener;

/**
 * Abstract base class for playbooks expecting only 2xx responses for base fields. Extends the {@link
 * BaseFieldsPlaybook} class and provides a constructor to initialize common dependencies for fuzzing
 * base fields with the expectation of only 2xx responses.
 */
public abstract class ExpectOnly2XXBaseFieldsPlaybook extends BaseFieldsPlaybook {

  /**
   * Constructor for initializing common dependencies for fuzzing base fields with the expectation
   * of only 2xx responses.
   *
   * @param sc The {@link ServiceCaller} used to make service calls.
   * @param lr The {@link TestCaseListener} for reporting test case events.
   * @param cp The {@link FilesArguments} for file-related arguments.
   */
  protected ExpectOnly2XXBaseFieldsPlaybook(
      ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
    super(sc, lr, cp);
  }

  @Override
  public ResponseCodeFamily getExpectedHttpCodeWhenRequiredFieldsAreFuzzed() {
    return ResponseCodeFamilyPredefined.TWOXX;
  }

  @Override
  public ResponseCodeFamily getExpectedHttpCodeWhenOptionalFieldsAreFuzzed() {
    return ResponseCodeFamilyPredefined.TWOXX;
  }

  @Override
  public ResponseCodeFamily getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern() {
    return ResponseCodeFamilyPredefined.TWOXX;
  }
}
