package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Playbook that sends a "happy" flow request with no fuzzing applied.
 */
@Singleton
@BodyPlaybook
public class HappyPathPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(HappyPathPlaybook.class);
    private final SimpleExecutor simpleExecutor;

    /**
     * Creates a new HappyPathPlaybook instance.
     *
     * @param simpleExecutor the executor
     */
    @Inject
    public HappyPathPlaybook(SimpleExecutor simpleExecutor) {
        this.simpleExecutor = simpleExecutor;
    }

    @Override
    public void run(PlaybookData data) {
        simpleExecutor.execute(SimpleExecutorContext.builder()
                .playbookData(data)
                .expectedResponseCode(ResponseCodeFamilyPredefined.TWOXX)
                .testCasePlaybook(this)
                .payload(data.getPayload())
                .scenario("Send a 'happy' flow request with all fields and all headers")
                .logger(logger)
                .build());
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public String description() {
        return "Send request with all fields and headers properly populated";
    }
}
