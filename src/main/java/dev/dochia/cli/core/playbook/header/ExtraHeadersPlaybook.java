package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

import static dev.dochia.cli.core.util.DSLWords.DOCHIA_FUZZY_HEADER;

/**
 * Adds an extra header for each request.
 */
@Singleton
@HeaderPlaybook
public class ExtraHeadersPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(ExtraHeadersPlaybook.class);

    private final SimpleExecutor simpleExecutor;

    /**
     * Creates a new instance.
     *
     * @param simpleExecutor executor used to run the fuzz logic
     */
    public ExtraHeadersPlaybook(SimpleExecutor simpleExecutor) {
        this.simpleExecutor = simpleExecutor;
    }

    @Override
    public void run(PlaybookData data) {
        Set<DochiaHeader> headerSet = new HashSet<>(data.getHeaders());
        headerSet.add(DochiaHeader.builder().name(DOCHIA_FUZZY_HEADER).required(false).value(DOCHIA_FUZZY_HEADER).build());

        simpleExecutor.execute(
                SimpleExecutorContext.builder()
                        .playbookData(data)
                        .expectedResponseCode(ResponseCodeFamilyPredefined.TWOXX)
                        .testCasePlaybook(this)
                        .logger(logger)
                        .scenario("Add an extra header inside the request: name [%s], value [%s]. ".formatted(DOCHIA_FUZZY_HEADER, DOCHIA_FUZZY_HEADER))
                        .headers(headerSet)
                        .build()
        );
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public String description() {
        return "Send happy path request and add extra header 'Dochia-Fuzzy-Header'";
    }
}