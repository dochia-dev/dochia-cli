package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.generator.simple.NumberGenerator;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.field.base.BaseBoundaryFieldPlaybook;
import dev.dochia.cli.core.report.TestCaseListener;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook that sends extreme negative numbers in integer fields.
 */
@Singleton
@FieldPlaybook
public class ExtremeNegativeNumbersInIntegerFieldsPlaybook extends BaseBoundaryFieldPlaybook {

    /**
     * Creates a new ExtremeNegativeNumbersInIntegerFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    public ExtremeNegativeNumbersInIntegerFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    public String typeOfDataSentToTheService() {
        return "extreme negative values";
    }

    @Override
    public List<String> getSchemaTypesThePlaybookWillApplyTo() {
        return List.of("integer");
    }

    @Override
    public Number getBoundaryValue(Schema schema) {
        return NumberGenerator.getExtremeNegativeIntegerValue(schema);
    }

    @Override
    public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
        return true;
    }

    @Override
    public String description() {
        return "Iterate through each integer field and send extreme negative values based on format constraints";
    }
}