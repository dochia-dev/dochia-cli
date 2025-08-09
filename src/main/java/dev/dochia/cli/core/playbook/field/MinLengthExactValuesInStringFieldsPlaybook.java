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
 * Playbook that min length exact values in string fields if 'minLength' property is defined.
 */
@Singleton
@FieldPlaybook
public class MinLengthExactValuesInStringFieldsPlaybook extends ExactValuesInFieldsPlaybook {

    /**
     * Creates a new MinLengthExactValuesInStringFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    public MinLengthExactValuesInStringFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    protected String exactValueTypeString() {
        return "minLength";
    }

    @Override
    public Function<Schema, Number> getExactMethod() {
        return Schema::getMinLength;
    }
}