package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.field.base.BaseBoundaryFieldPlaybook;
import dev.dochia.cli.core.generator.simple.NumberGenerator;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook that sends extreme negative numbers in decimal fields.
 */
@Singleton
@FieldPlaybook
public class ExtremeNegativeNumbersInDecimalFieldsPlaybook extends BaseBoundaryFieldPlaybook {

    /**
     * Creates a new ExtremeNegativeNumbersInDecimalFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    public ExtremeNegativeNumbersInDecimalFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    public String typeOfDataSentToTheService() {
        return "extreme negative values";
    }

    @Override
    public List<String> getSchemaTypesThePlaybookWillApplyTo() {
        return List.of("number");
    }

    @Override
    public Object getBoundaryValue(Schema schema) {
        return NumberGenerator.getExtremeNegativeDecimalValue(schema);
    }

    @Override
    public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
        return true;
    }

    @Override
    public String description() {
        return String.format("iterate through each Number field and send %s for no format, %s for float and %s for double", NumberGenerator.MOST_NEGATIVE_DECIMAL, -Float.MAX_VALUE, -Double.MAX_VALUE);
    }
}