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
 * Playbook that sends strings in numeric fields.
 */
@Singleton
@FieldPlaybook
public class StringsInNumericFieldsPlaybook extends BaseBoundaryFieldPlaybook {

    /**
     * Creates a new StringsInNumericFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    public StringsInNumericFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    public List<String> getSchemaTypesThePlaybookWillApplyTo() {
        return List.of("integer", "number");
    }

    @Override
    public String getBoundaryValue(Schema schema) {
        return StringGenerator.generateRandomString();
    }

    @Override
    public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
        return true;
    }

    @Override
    public String typeOfDataSentToTheService() {
        return "strings in numeric fields";
    }

    @Override
    public String description() {
        return "iterate through each Integer (int, long) and Number field (float, double) and send requests having the `fuzz` string value";
    }
}