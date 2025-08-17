package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.playbook.field.base.ExpectOnly4XXBaseFieldsPlaybook;
import dev.dochia.cli.core.generator.simple.StringGenerator;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.List;

/**
 * Playbook that sends very large  strings in string fields. Size of the large
 * strings is controlled by the {@code --largeStringsSize} argument.
 */
@Singleton
@FieldPlaybook
public class VeryLargeStringsInFieldsPlaybook extends ExpectOnly4XXBaseFieldsPlaybook {
    private final ProcessingArguments processingArguments;

    /**
     * Creates a new VeryLargeStringsInFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     * @param pa to get the size of the large strings
     */
    public VeryLargeStringsInFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp, ProcessingArguments pa) {
        super(sc, lr, cp);
        this.processingArguments = pa;
    }

    @Override
    public String typeOfDataSentToTheService() {
        return "very large string values";
    }

    @Override
    protected List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
        return Collections.singletonList(
                FuzzingStrategy.replace().withData(
                        StringGenerator.generateLargeString(processingArguments.getLargeStringsSize() / 4)));
    }

    @Override
    protected boolean shouldCheckForFuzzedValueMatchingPattern() {
        return false;
    }

    @Override
    protected boolean shouldMatchResponseSchema(PlaybookData data) {
        return HttpMethod.requiresBody(data.getMethod());
    }

    @Override
    protected boolean shouldMatchContentType(PlaybookData data) {
        return HttpMethod.requiresBody(data.getMethod());
    }

    @Override
    public String description() {
        return "Iterate through each string field and send very large values (40000 characters)";
    }
}