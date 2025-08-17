package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.util.DSLWords;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Send duplicate headers either duplicating an existing header or duplicating a dummy one.
 */
@Singleton
@HeaderPlaybook
public class DuplicateHeadersPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(DuplicateHeadersPlaybook.class);
    private final SimpleExecutor simpleExecutor;

    /**
     * Creates a new instance.
     *
     * @param simpleExecutor executor used to run the fuzz logic
     */
    public DuplicateHeadersPlaybook(SimpleExecutor simpleExecutor) {
        this.simpleExecutor = simpleExecutor;
    }

    @Override
    public void run(PlaybookData data) {
        List<DochiaHeader> headers = new ArrayList<>(data.getHeaders());
        DochiaHeader header = DochiaHeader.builder().name(DSLWords.DOCHIA_FUZZY_HEADER).required(false).value(DSLWords.DOCHIA_FUZZY_HEADER).build();

        if (headers.isEmpty()) {
            logger.skip("No headers to fuzz. Adding default: %s".formatted(DSLWords.DOCHIA_FUZZY_HEADER));
            headers.add(header);
        }

        for (DochiaHeader dochiaHeader : headers) {
            List<DochiaHeader> finalHeadersList = new ArrayList<>(headers);
            finalHeadersList.add(dochiaHeader.copy());
            simpleExecutor.execute(
                    SimpleExecutorContext.builder()
                            .headers(finalHeadersList)
                            .playbookData(data)
                            .logger(logger)
                            .expectedResponseCode(ResponseCodeFamilyPredefined.FOURXX)
                            .scenario("Add a duplicate header inside the request: name [%s], value [%s]. All other details are similar to a happy flow".formatted(dochiaHeader.getName(), dochiaHeader.getTruncatedValue()))
                            .testCasePlaybook(this)
                            .build());
        }
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public String description() {
        return "Send happy path request and duplicate an existing header";
    }
}