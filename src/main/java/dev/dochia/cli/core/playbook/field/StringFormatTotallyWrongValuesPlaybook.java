package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.field.base.BaseBoundaryFieldPlaybook;
import dev.dochia.cli.core.generator.format.api.InvalidDataFormat;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook that iterates through each String field and get its 'format' value (i.e. email, ip, uuid, date, datetime, etc.)
 * and sends requests with values which are totally wrong (i.e. abcd for email, 1244. for ip, etc.).
 */
@Singleton
@FieldPlaybook
public class StringFormatTotallyWrongValuesPlaybook extends BaseBoundaryFieldPlaybook {
    private final InvalidDataFormat invalidDataFormat;

    /**
     * Creates a new StringFormatTotallyWrongValuesPlaybook instance.
     *
     * @param sc                the service caller
     * @param lr                the test case listener
     * @param cp                files arguments
     * @param invalidDataFormat provider for invalid data formats
     */
    public StringFormatTotallyWrongValuesPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp, InvalidDataFormat invalidDataFormat) {
        super(sc, lr, cp);
        this.invalidDataFormat = invalidDataFormat;
    }

    @Override
    public String typeOfDataSentToTheService() {
        return "totally wrong values according to supplied format";
    }

    @Override
    public List<String> getSchemaTypesThePlaybookWillApplyTo() {
        return List.of("string");
    }

    @Override
    public String getBoundaryValue(Schema schema) {
        return invalidDataFormat.generator(schema, "").getTotallyWrongValue();
    }

    @Override
    public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
        return true;
    }

    @Override
    public String description() {
        return "Iterate through each formatted string field and send completely invalid values for the format";
    }
}
