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
import java.util.stream.Collectors;

/**
 * Sends request with headers defined in the OpenAPI specs removed.
 */
@Singleton
@HeaderPlaybook
public class RemoveHeadersPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(RemoveHeadersPlaybook.class);
    private final SimpleExecutor simpleExecutor;

    /**
     * Creates a new instance.
     *
     * @param simpleExecutor executor used to run the fuzz logic
     */
    public RemoveHeadersPlaybook(SimpleExecutor simpleExecutor) {
        this.simpleExecutor = simpleExecutor;
    }

    @Override
    public void run(PlaybookData data) {
        if (data.getHeaders().isEmpty()) {
            logger.skip("No headers to fuzz");
            return;
        }
        Set<Set<DochiaHeader>> headersCombination = PlaybookData.SetFuzzingStrategy.powerSet(data.getHeaders());
        Set<DochiaHeader> mandatoryHeaders = data.getHeaders().stream().filter(DochiaHeader::isRequired).collect(Collectors.toSet());

        for (Set<DochiaHeader> headersSubset : headersCombination) {
            boolean anyMandatoryHeaderRemoved = this.isAnyMandatoryHeaderRemoved(headersSubset, mandatoryHeaders);

            simpleExecutor.execute(
                    SimpleExecutorContext.builder()
                            .logger(logger)
                            .testCasePlaybook(this)
                            .playbookData(data)
                            .headers(headersSubset)
                            .scenario("Send only the following headers: %s plus any authentication headers.".formatted(headersSubset))
                            .expectedResponseCode(ResponseCodeFamilyPredefined.getResultCodeBasedOnRequiredFieldsRemoved(anyMandatoryHeaderRemoved))
                            .expectedResult(" as mandatory headers [%s] removed".formatted(anyMandatoryHeaderRemoved ? "were" : "were not"))
                            .addUserHeaders(false)
                            .build()
            );
        }
    }

    private boolean isAnyMandatoryHeaderRemoved(Set<DochiaHeader> headersSubset, Set<DochiaHeader> requiredHeaders) {
        Set<DochiaHeader> intersection = new HashSet<>(requiredHeaders);
        intersection.retainAll(headersSubset);
        return intersection.size() != requiredHeaders.size();
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public String description() {
        return "Iterate through each header and remove different combinations";
    }
}
