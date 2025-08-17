package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Playbook that will bypass common authentication headers.
 */
@Singleton
@BodyPlaybook
public class BypassAuthenticationPlaybook implements TestCasePlaybook {
    private static final List<String> AUTH_HEADERS = Arrays.asList("cookie", "authorization", "authorisation", "token", "jwt", "apikey", "secret", "secretkey", "apisecret", "apitoken", "appkey", "appid");
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(BypassAuthenticationPlaybook.class);
    private final FilesArguments filesArguments;
    private final SimpleExecutor simpleExecutor;

    /**
     * Creates a new BypassAuthenticationPlaybook instance.
     *
     * @param ce             the executor
     * @param filesArguments files arguments
     */
    public BypassAuthenticationPlaybook(SimpleExecutor ce, FilesArguments filesArguments) {
        this.simpleExecutor = ce;
        this.filesArguments = filesArguments;
    }

    @Override
    public void run(PlaybookData data) {
        Set<String> authenticationHeaders = this.getAuthenticationHeaderProvided(data);
        if (authenticationHeaders.isEmpty()) {
            logger.skip("No authentication header provided.");
            return;
        }
        simpleExecutor.execute(
                SimpleExecutorContext.builder()
                        .testCasePlaybook(this)
                        .logger(logger)
                        .playbookData(data)
                        .payload(data.getPayload())
                        .scenario("Send a happy flow bypassing authentication. Removed headers " + authenticationHeaders)
                        .expectedResponseCode(ResponseCodeFamilyPredefined.FOURXX_AA)
                        .skippedHeaders(authenticationHeaders)
                        .build());
    }

    Set<String> getAuthenticationHeaderProvided(PlaybookData data) {
        Set<String> authenticationHeadersInContract = data.getHeaders().stream().map(DochiaHeader::getName)
                .filter(this::isAuthenticationHeader).collect(Collectors.toSet());
        Set<String> authenticationHeadersInFile = filesArguments.getHeaders(data.getPath()).keySet()
                .stream().filter(this::isAuthenticationHeader)
                .collect(Collectors.toSet());

        return Stream.of(authenticationHeadersInContract, authenticationHeadersInFile).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    private boolean isAuthenticationHeader(String header) {
        return AUTH_HEADERS.stream().anyMatch(authHeader -> header.toLowerCase(Locale.ROOT).replaceAll("[-_]", "").contains(authHeader));
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public String description() {
        return " Check if authentication headers are supplied and attempt requests without them";
    }
}
