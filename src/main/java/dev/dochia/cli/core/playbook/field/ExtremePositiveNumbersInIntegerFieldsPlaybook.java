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
 * Playbook that will send extreme positive numbers in integer fields.
 */
@Singleton
@FieldPlaybook
public class ExtremePositiveNumbersInIntegerFieldsPlaybook extends BaseBoundaryFieldPlaybook {

    /**
     * Creates a new ExtremePositiveNumbersInIntegerFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    public ExtremePositiveNumbersInIntegerFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    public String typeOfDataSentToTheService() {
        return "extreme positive values";
    }

    @Override
    public List<String> getSchemaTypesThePlaybookWillApplyTo() {
        return List.of("integer");
    }

    @Override
    public Number getBoundaryValue(Schema schema) {
        return NumberGenerator.getExtremePositiveIntegerValue(schema);
    }

    @Override
    public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
        return true;
    }

    @Override
    public String description() {
        return "Iterate through each integer field and send extreme positive values based on format constraints";
    }
}