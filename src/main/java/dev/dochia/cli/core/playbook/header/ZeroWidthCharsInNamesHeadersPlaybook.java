package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Singleton;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fuzzes HTTP headers by injecting zero-width characters in the names.
 */
@HeaderPlaybook
@Singleton
public class ZeroWidthCharsInNamesHeadersPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(ZeroWidthCharsInNamesHeadersPlaybook.class);

    private final SimpleExecutor simpleExecutor;

    /**
     * Creates a new instance.
     *
     * @param simpleExecutor executor used to run the fuzz logic
     */
    public ZeroWidthCharsInNamesHeadersPlaybook(SimpleExecutor simpleExecutor) {
        this.simpleExecutor = simpleExecutor;
    }

    @Override
    public void run(PlaybookData data) {
        if (data.getHeaders().isEmpty()) {
            return;
        }
        for (String zeroWidthChar : UnicodeGenerator.getZwCharsSmallListHeaders()) {
            Set<DochiaHeader> clonedHeaders = data.getHeaders().stream()
                    .map(dochiaHeader -> DochiaHeader.builder()
                            .name(CommonUtils.insertInTheMiddle(dochiaHeader.getName(), zeroWidthChar, true))
                            .value(dochiaHeader.getValue())
                            .required(dochiaHeader.isRequired())
                            .build())
                    .collect(Collectors.toSet());

            simpleExecutor.execute(
                    SimpleExecutorContext.builder()
                            .playbookData(data)
                            .expectedResponseCode(ResponseCodeFamilyPredefined.FOURXX)
                            .testCasePlaybook(this)
                            .matchResponseContentType(false)
                            .matchResponseResult(false)
                            .logger(logger)
                            .scenario("Inject zero-width characters in the header names")
                            .headers(clonedHeaders)
                            .build()
            );
        }
    }


    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public String description() {
        return "Inject zero-width characters in header names";
    }
}
