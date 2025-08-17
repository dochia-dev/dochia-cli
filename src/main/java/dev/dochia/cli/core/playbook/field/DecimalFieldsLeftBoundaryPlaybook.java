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
 * Playbook that sends left boundary values in decimal fields.
 */
@Singleton
@FieldPlaybook
public class DecimalFieldsLeftBoundaryPlaybook extends BaseBoundaryFieldPlaybook {

    /**
     * Creates a new DecimalFieldsLeftBoundaryPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    public DecimalFieldsLeftBoundaryPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    public List<String> getSchemaTypesThePlaybookWillApplyTo() {
        return List.of("number");
    }

    @Override
    public Number getBoundaryValue(Schema schema) {
        return NumberGenerator.generateLeftBoundaryDecimalValue(schema);
    }

    @Override
    protected boolean isPlaybookApplicable(PlaybookData data, String fuzzedField) {
        return filesArguments.getRefData(data.getPath()).get(fuzzedField) == null;
    }

    @Override
    public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
        return true;
    }

    @Override
    public String description() {
        return "Iterate through each number field (float/double) and send out-of-range values on the left boundary";
    }
}