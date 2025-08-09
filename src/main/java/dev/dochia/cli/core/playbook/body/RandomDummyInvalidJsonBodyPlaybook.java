package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.List;

/**
 * Playbook that sends dummy invalid JSON bodies.
 */
@Singleton
@BodyPlaybook
public class RandomDummyInvalidJsonBodyPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(getClass());
    private final SimpleExecutor simpleExecutor;
    private static final List<String> PAYLOADS = List.of("{0}", "{0.0}", "[{}]", "{$}",
            """ 
                    {"circularRef": {"self": {"$ref": "#/circularRef"}}}
                    """,
            """
                    {"backslash": "\\"}
                     """,
            """
                    {"ünicode": "ünicode"}
                    """,
            """
                    "{"unexpected" $ "token": "value"}
                    """,
            """
                    {\u0000:\u0000}
                    """,
            """
                    {"\u0000":"\u0000"}
                    """,
            """
                    {"␀":"␀"}
                    """,
            """
                    {␀:␀}
                    """);

    /**
     * Create a new RandomDummyInvalidJsonBodyPlaybook instance.
     *
     * @param ce the executor
     */
    @Inject
    RandomDummyInvalidJsonBodyPlaybook(SimpleExecutor ce) {
        this.simpleExecutor = ce;
    }

    @Override
    public void run(PlaybookData data) {
        for (String payload : PAYLOADS) {
            simpleExecutor.execute(
                    SimpleExecutorContext.builder()
                            .expectedResponseCode(ResponseCodeFamilyPredefined.FOURXX)
                            .playbookData(data)
                            .logger(logger)
                            .replaceRefData(false)
                            .scenario("Send %s as invalid json request body".formatted(payload))
                            .testCasePlaybook(this)
                            .payload(payload)
                            .build());
        }
    }

    @Override
    public String description() {
        return "send a request with dummy invalid json body";
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public List<HttpMethod> skipForHttpMethods() {
        return Arrays.asList(HttpMethod.GET, HttpMethod.DELETE);
    }
}