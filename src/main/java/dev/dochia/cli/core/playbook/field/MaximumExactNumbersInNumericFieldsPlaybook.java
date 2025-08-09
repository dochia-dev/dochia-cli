package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.field.base.ExactValuesInFieldsPlaybook;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.function.Function;

/**
 * Playbook that sends maximum exact numbers in numeric fields if they have 'maximum' defined.
 */
@Singleton
@FieldPlaybook
public class MaximumExactNumbersInNumericFieldsPlaybook extends ExactValuesInFieldsPlaybook {

    /**
     * Creates a new MaximumExactNumbersInNumericFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    public MaximumExactNumbersInNumericFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    protected String exactValueTypeString() {
        return "maximum";
    }

    @Override
    protected Function<Schema, Number> getExactMethod() {
        return Schema::getMaximum;
    }

    @Override
    public List<String> getSchemaTypesThePlaybookWillApplyTo() {
        return List.of("number", "integer");
    }

    @Override
    public Object getBoundaryValue(Schema schema) {
        return getExactMethod().apply(schema);
    }

    @Override
    public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
        Schema schema = data.getRequestPropertyTypes().get(fuzzedField);
        boolean isRefDataField = filesArguments.getRefData(data.getPath()).get(fuzzedField) != null;
        return !isRefDataField && getExactMethod().apply(schema) != null;
    }
}
