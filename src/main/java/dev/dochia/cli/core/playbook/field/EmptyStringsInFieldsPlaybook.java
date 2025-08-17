package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.playbook.field.base.Expect4XXForRequiredBaseFieldsPlaybook;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.List;

/**
 * Playbook that sends empty values in string fields.
 */
@Singleton
@FieldPlaybook
public class EmptyStringsInFieldsPlaybook extends Expect4XXForRequiredBaseFieldsPlaybook {
    private final FilterArguments filterArguments;

    /**
     * Creates a new EmptyStringsInFieldsPlaybook instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     * @param fa filter arguments
     */
    public EmptyStringsInFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp, FilterArguments fa) {
        super(sc, lr, cp);
        this.filterArguments = fa;
    }

    @Override
    protected String typeOfDataSentToTheService() {
        return "empty strings";
    }

    @Override
    protected List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
        return Collections.singletonList(FuzzingStrategy.replace().withData(""));
    }

    @Override
    protected ResponseCodeFamily getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern() {
        return ResponseCodeFamilyPredefined.FOURXX;
    }

    @Override
    public boolean isPlaybookApplicable(PlaybookData data, String fuzzedField) {
        return HttpMethod.requiresBody(data.getMethod()) || data.isQueryParam(fuzzedField);
    }

    @Override
    public List<String> skipForFields() {
        return filterArguments.getSkipFields();
    }

    @Override
    public String description() {
        return "Iterate through each field and send empty string values";
    }
}
