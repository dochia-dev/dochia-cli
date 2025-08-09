package dev.dochia.cli.core.playbook.field.only;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.playbook.field.base.Expect4XXForRequiredBaseFieldsPlaybook;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;

/**
 * Base class for playbooks sending only invisible chars in fields.
 */
public abstract class InvisibleCharsOnlyTrimValidatePlaybook extends Expect4XXForRequiredBaseFieldsPlaybook {
    private final FilterArguments filterArguments;

    /**
     * Constructor for initializing common dependencies for fuzzing fields with invisible chars.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     * @param fa filter arguments
     */
    protected InvisibleCharsOnlyTrimValidatePlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp, FilterArguments fa) {
        super(sc, lr, cp);
        this.filterArguments = fa;
    }

    @Override
    protected List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
        Schema<?> schema = data.getRequestPropertyTypes().get(fuzzedField);
        return this.getInvisibleChars().stream()
                .map(value -> FuzzingStrategy.getFuzzStrategyWithRepeatedCharacterReplacingValidValue(schema, value)).toList();
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern() {
        return ResponseCodeFamilyPredefined.FOURXX;
    }

    /**
     * Supplied skipped fields are skipped when we only sent invalid data.
     *
     * @return the list with skipped fields.
     */
    @Override
    public List<String> skipForFields() {
        return filterArguments.getSkipFields();
    }

    @Override
    public String description() {
        return "iterate through each field and send  " + this.typeOfDataSentToTheService();
    }

    /**
     * Returns the actual list of invisible chars to be used for fuzzing.
     *
     * @return a list of invisible chars to be used for fuzzing.
     */
    abstract List<String> getInvisibleChars();
}