package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.field.base.ExactValuesInFieldsPlaybook;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.report.TestCaseListener;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.function.Function;

/**
 * Playbook that sends max length exact values in string fields if they have 'maxLength' defined.
 */
@Singleton
@FieldPlaybook
public class MaxLengthExactValuesInStringFieldsPlaybook extends ExactValuesInFieldsPlaybook {

    /**
     * Creates a new MaxLengthExactValuesInStringFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    public MaxLengthExactValuesInStringFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    protected String exactValueTypeString() {
        return "maxLength";
    }

    @Override
    public Function<Schema, Number> getExactMethod() {
        return Schema::getMaxLength;
    }
}