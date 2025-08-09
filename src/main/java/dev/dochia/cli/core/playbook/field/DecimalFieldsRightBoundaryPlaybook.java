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
 * Playbook that sends right boundary values in decimal fields.
 */
@Singleton
@FieldPlaybook
public class DecimalFieldsRightBoundaryPlaybook extends BaseBoundaryFieldPlaybook {

    /**
     * Creates a new DecimalFieldsRightBoundaryPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param fa files arguments
     */
    public DecimalFieldsRightBoundaryPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments fa) {
        super(sc, lr, fa);
    }

    @Override
    public List<String> getSchemaTypesThePlaybookWillApplyTo() {
        return List.of("number");
    }

    @Override
    protected boolean isPlaybookApplicable(PlaybookData data, String fuzzedField) {
        return filesArguments.getRefData(data.getPath()).get(fuzzedField) == null;
    }

    @Override
    public Number getBoundaryValue(Schema schema) {
        return NumberGenerator.generateRightBoundaryDecimalValue(schema);
    }

    @Override
    public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
        return true;
    }

    @Override
    public String description() {
        return "iterate through each Number field (either float or double) and send outside the range values on the right side in the targeted field";
    }
}