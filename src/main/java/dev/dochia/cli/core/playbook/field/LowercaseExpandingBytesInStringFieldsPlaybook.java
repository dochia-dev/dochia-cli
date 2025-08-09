package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.field.base.TwoXXorFourXXReplaceStringsPlaybook;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.util.CommonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.function.BiFunction;

/**
 * The playbook that sends values that will expand the byte representation when lower cased.
 */
@Singleton
@FieldPlaybook
public class LowercaseExpandingBytesInStringFieldsPlaybook extends TwoXXorFourXXReplaceStringsPlaybook {
    /**
     * Constructor for initializing common dependencies for fuzzing fields.
     *
     * @param testCaseListener       the test case listener
     * @param fieldsIteratorExecutor the executor
     */
    public LowercaseExpandingBytesInStringFieldsPlaybook(TestCaseListener testCaseListener, FieldsIteratorExecutor fieldsIteratorExecutor) {
        super(testCaseListener, fieldsIteratorExecutor);
    }

    @Override
    protected BiFunction<Schema<?>, String, List<Object>> fuzzValueProducer() {
        return (schema, s) -> {
            int schemaLength = schema.getMaxLength() != null ? schema.getMaxLength() : 10;
            return List.of(CommonUtils.selectRandom(UnicodeGenerator.getLowercaseExpandingBytes(), schemaLength));
        };
    }

    @Override
    protected String typesOfDataSentToTheService() {
        return "values that will expand the byte representation when lowercased";
    }

    @Override
    public String description() {
        return "iterate to string fields and send values that expand the byte representation when lowercased";
    }
}
