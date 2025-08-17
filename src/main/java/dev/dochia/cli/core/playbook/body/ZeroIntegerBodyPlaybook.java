package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.model.PlaybookData;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/** Playbook that sends the zero integer as a body. */
@Singleton
@BodyPlaybook
public class ZeroIntegerBodyPlaybook extends BaseHttpWithPayloadSimplePlaybook {

  /**
   * Creates a new ZeroIntegerBodyPlaybook instance.
   *
   * @param executor the executor
   */
  @Inject
  public ZeroIntegerBodyPlaybook(SimpleExecutor executor) {
    super(executor);
  }

  @Override
  protected String getPayload(PlaybookData data) {
    return "0";
  }

  @Override
  protected String getScenario() {
    return "Send a request with integer 0 (zero) as body";
  }

  @Override
  public String description() {
    return "Send request with integer 0 as body";
  }
}
