package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.playbook.field.base.ExpectOnly4XXBaseFieldsPlaybook;
import dev.dochia.cli.core.generator.simple.NumberGenerator;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import dev.dochia.cli.core.util.DochiaModelUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.List;

/**
 * Playbook that sends very large integers in numeric fields. Size of the large
 * integer is controlled by the {@code --largeStringsSize} argument.
 */
@Singleton
@FieldPlaybook
public class VeryLargeIntegersInNumericFieldsPlaybook extends ExpectOnly4XXBaseFieldsPlaybook {

    final ProcessingArguments processingArguments;

    /**
     * Creates a new VeryLargeIntegersInNumericFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     * @param pa to get the size of the large integers
     */
    public VeryLargeIntegersInNumericFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp, ProcessingArguments pa) {
        super(sc, lr, cp);
        this.processingArguments = pa;
    }

    @Override
    public String typeOfDataSentToTheService() {
        return "very large numbers";
    }

    @Override
    protected List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
        Schema<?> fieldSchema = data.getRequestPropertyTypes().get(fuzzedField);
        if (DochiaModelUtils.isNumberSchema(fieldSchema) || DochiaModelUtils.isIntegerSchema(fieldSchema)) {
            return Collections.singletonList(FuzzingStrategy.replace().withData(this.getTheActualData()));
        } else {
            return List.of(FuzzingStrategy.skip().withData("field is not numeric"));
        }
    }

    public String getTheActualData() {
        return NumberGenerator.generateVeryLargeInteger(processingArguments.getLargeStringsSize() / 4);
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
        return "iterate through each numeric field and send very large numbers (40000 characters)";
    }
}