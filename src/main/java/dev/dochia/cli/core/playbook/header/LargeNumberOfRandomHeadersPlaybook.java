package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.header.base.BaseRandomHeadersPlaybook;
import dev.dochia.cli.core.report.TestCaseListener;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.function.Function;

/**
 * Sends a large number of random headers with random names and values.
 */
@Singleton
@HeaderPlaybook
public class LargeNumberOfRandomHeadersPlaybook extends BaseRandomHeadersPlaybook {

    /**
     * Creates a new instance.
     *
     * @param simpleExecutor      the executor used to run the fuzz logic
     * @param testCaseListener    listener used to report test case progress
     * @param processingArguments used to hold configuration for how many headers to send
     */
    public LargeNumberOfRandomHeadersPlaybook(SimpleExecutor simpleExecutor, TestCaseListener testCaseListener, ProcessingArguments processingArguments) {
        super(simpleExecutor, testCaseListener, processingArguments);
    }

    @Override
    public String description() {
        return String.format("send a 'happy' flow request with %s extra random headers", super.processingArguments.getRandomHeadersNumber());
    }

    @Override
    protected Function<Integer, String> randomHeadersValueFunction() {
        return RandomStringUtils.secure()::next;
    }
}
