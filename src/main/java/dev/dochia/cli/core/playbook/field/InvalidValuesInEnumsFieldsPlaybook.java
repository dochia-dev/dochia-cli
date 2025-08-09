package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.playbook.field.base.BaseBoundaryFieldPlaybook;
import dev.dochia.cli.core.generator.simple.StringGenerator;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Playbook that sends invalid values in enums. There is an argument {@code --allowInvalidEnumValues}
 * which will influence the expected response codes by this playbook.
 */
@Singleton
@FieldPlaybook
public class InvalidValuesInEnumsFieldsPlaybook extends BaseBoundaryFieldPlaybook {
    final ProcessingArguments processingArguments;

    /**
     * Creates a new InvalidValuesInEnumsFieldsPlaybook instance.
     *
     * @param sc                  the service caller
     * @param lr                  the test case listener
     * @param cp                  files arguments
     * @param processingArguments to get the {@code --allowInvalidEnumValues} argument value
     */
    public InvalidValuesInEnumsFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp, ProcessingArguments processingArguments) {
        super(sc, lr, cp);
        this.processingArguments = processingArguments;
    }

    @Override
    public String typeOfDataSentToTheService() {
        return "invalid ENUM values";
    }

    @Override
    public List<String> getSchemaTypesThePlaybookWillApplyTo() {
        return List.of("string");
    }

    @Override
    public String getBoundaryValue(Schema schema) {
        if (schema.getEnum() != null) {
            int length = String.valueOf(schema.getEnum().getFirst()).length();
            return StringGenerator.generate("[A-Z]+", length, length);
        }
        return null;
    }

    @Override
    public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
        Schema schema = data.getRequestPropertyTypes().get(fuzzedField);
        return schema.getEnum() != null;
    }

    @Override
    public String description() {
        return "iterate through each ENUM field and send invalid values";
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenRequiredFieldsAreFuzzed() {
        return processingArguments.isAllowInvalidEnumValues() ? ResponseCodeFamilyPredefined.TWOXX : ResponseCodeFamilyPredefined.FOURXX;
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenOptionalFieldsAreFuzzed() {
        return processingArguments.isAllowInvalidEnumValues() ? ResponseCodeFamilyPredefined.TWOXX : ResponseCodeFamilyPredefined.FOURXX;
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern() {
        return processingArguments.isAllowInvalidEnumValues() ? ResponseCodeFamilyPredefined.TWOXX : ResponseCodeFamilyPredefined.FOURXX;
    }
}
