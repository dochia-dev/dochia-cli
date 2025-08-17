package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.playbook.field.base.ExpectOnly4XXBaseFieldsPlaybook;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook that sends very large unicode strings in string fields. Size of the large unicode
 * strings is controlled by the {@code --largeStringsSize} argument.
 */
@Singleton
@FieldPlaybook
public class VeryLargeUnicodeStringsInFieldsPlaybook extends ExpectOnly4XXBaseFieldsPlaybook {

    private final ProcessingArguments processingArguments;

    /**
     * Creates a new VeryLargeUnicodeStringsInFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     * @param pa to get the size of the large strings
     */
    public VeryLargeUnicodeStringsInFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp, ProcessingArguments pa) {
        super(sc, lr, cp);
        this.processingArguments = pa;
    }

    @Override
    public String typeOfDataSentToTheService() {
        return "very large unicode values";
    }

    @Override
    protected List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
        return FuzzingStrategy.getLargeValuesStrategy(processingArguments.getLargeStringsSize());
    }

    @Override
    protected boolean shouldMatchContentType(PlaybookData data) {
        return HttpMethod.requiresBody(data.getMethod());
    }

    @Override
    protected boolean shouldMatchResponseSchema(PlaybookData data) {
        return HttpMethod.requiresBody(data.getMethod());
    }

    @Override
    protected boolean shouldCheckForFuzzedValueMatchingPattern() {
        return false;
    }

    @Override
    public String description() {
        return "Iterate through each field and send very large random unicode values";
    }
}