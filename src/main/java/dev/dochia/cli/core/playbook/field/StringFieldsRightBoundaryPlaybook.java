package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.field.base.BaseBoundaryFieldPlaybook;
import dev.dochia.cli.core.generator.simple.StringGenerator;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook class targeting right boundary conditions for string fields.
 * Extends the {@link BaseBoundaryFieldPlaybook} class and provides a constructor
 * to initialize common dependencies for fuzzing string fields with the expectation of right boundary conditions.
 */
@Singleton
@FieldPlaybook
public class StringFieldsRightBoundaryPlaybook extends BaseBoundaryFieldPlaybook {

    /**
     * Constructor for initializing common dependencies for fuzzing string fields with the expectation of right boundary conditions.
     *
     * @param sc The {@link ServiceCaller} used to make service calls.
     * @param lr The {@link TestCaseListener} for reporting test case events.
     * @param cp The {@link FilesArguments} for file-related arguments.
     */
    public StringFieldsRightBoundaryPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    public List<String> getSchemaTypesThePlaybookWillApplyTo() {
        return List.of("string");
    }

    @Override
    public String getBoundaryValue(Schema schema) {
        return StringGenerator.generateRightBoundString(schema);
    }

    @Override
    public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
        return true;
    }


    @Override
    public String description() {
        return "Iterate through each string field and send out-of-range values on the right boundary";
    }
}
