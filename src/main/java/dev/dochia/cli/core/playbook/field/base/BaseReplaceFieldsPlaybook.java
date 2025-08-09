package dev.dochia.cli.core.playbook.field.base;

import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutorContext;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import lombok.Builder;

/**
 * Base class for playbooks that replace valid field values with fuzzed values.
 */
public abstract class BaseReplaceFieldsPlaybook implements TestCasePlaybook {
    /**
     * Logger used across all sub-classes.
     */
    protected final PrettyLogger logger = PrettyLoggerFactory.getLogger(getClass());
    private final FieldsIteratorExecutor executor;

    /**
     * Constructor for initializing common dependencies for fuzzing fields.
     *
     * @param ce the executor
     */
    protected BaseReplaceFieldsPlaybook(FieldsIteratorExecutor ce) {
        this.executor = ce;
    }

    @Override
    public void run(PlaybookData data) {
        BaseReplaceFieldsContext context = this.getContext(data);
        executor.execute(
                FieldsIteratorExecutorContext.builder()
                        .scenario("Replace %s fields with %s values. ".formatted(context.replaceWhat, context.replaceWith))
                        .playbookData(data).fuzzingStrategy(FuzzingStrategy.replace())
                        .expectedResponseCode(context.expectedResponseCode)
                        .skipMessage(context.skipMessage)
                        .fieldFilter(context.fieldFilter)
                        .fuzzValueProducer(context.fuzzValueProducer)
                        .replaceRefData(context.replaceRefData)
                        .simpleReplaceField(true)
                        .logger(logger)
                        .testCasePlaybook(this)
                        .build());
    }

    @Override
    public String description() {
        BaseReplaceFieldsContext context = this.getContext(null);
        return "iterate through each %s field and replace it with %s values".formatted(context.replaceWhat, context.replaceWith);
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public List<HttpMethod> skipForHttpMethods() {
        return List.of(HttpMethod.HEAD, HttpMethod.GET, HttpMethod.DELETE);
    }

    /**
     * Context for subclasses to provide.
     */
    @Builder
    public static class BaseReplaceFieldsContext {
        private final String replaceWhat;
        private final String replaceWith;
        private final String skipMessage;
        private final Predicate<String> fieldFilter;
        private final BiFunction<Schema<?>, String, List<Object>> fuzzValueProducer;
        @Builder.Default
        private final ResponseCodeFamily expectedResponseCode = ResponseCodeFamilyPredefined.FOURXX;
        private final boolean replaceRefData;
    }

    /**
     * Override to provide the actual context for the playbook to run.
     *
     * @param data the fuzzing data object
     * @return context for the playbook to run
     */
    public abstract BaseReplaceFieldsContext getContext(PlaybookData data);
}