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
 * Playbook that sends decimal numbers in numeric fields.
 */
@Singleton
@FieldPlaybook
public class DecimalNumbersInIntegerFieldsPlaybook extends BaseBoundaryFieldPlaybook {

    /**
     * Creates a new DecimalNumbersInIntegerFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    public DecimalNumbersInIntegerFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    public String typeOfDataSentToTheService() {
        return "decimal values in integer fields";
    }


    @Override
    public List<String> getSchemaTypesThePlaybookWillApplyTo() {
        return List.of("integer");
    }

    @Override
    public Number getBoundaryValue(Schema schema) {
        return NumberGenerator.generateDecimalValue(schema);
    }

    @Override
    public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
        return true;
    }

    @Override
    public String description() {
        return "Iterate through each integer field and send decimal values";
    }
}