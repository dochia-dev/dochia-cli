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
 * Playbook that sends left boundary values for integer fields.
 */
@Singleton
@FieldPlaybook
public class IntegerFieldsLeftBoundaryPlaybook extends BaseBoundaryFieldPlaybook {

    /**
     * Creates a new IntegerFieldsLeftBoundaryPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param fa files arguments
     */
    public IntegerFieldsLeftBoundaryPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments fa) {
        super(sc, lr, fa);
    }

    @Override
    public List<String> getSchemaTypesThePlaybookWillApplyTo() {
        return List.of("integer");
    }

    @Override
    public Number getBoundaryValue(Schema schema) {
        return NumberGenerator.generateLeftBoundaryIntegerValue(schema);
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
        return "iterate through each Integer field and send outside the range values on the left side";
    }
}
