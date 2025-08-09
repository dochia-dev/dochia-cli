package dev.dochia.cli.core.playbook.field.base;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.generator.format.api.InvalidDataFormat;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import dev.dochia.cli.core.util.JsonUtils;
import io.swagger.v3.oas.models.media.Schema;
import java.util.Collections;
import java.util.List;

/**
 * Extend this class to provide concrete boundary values to be used for fuzzing.
 * The assumption is that expected response is a 4XX code when sending out of boundary values for the fuzzed fields.
 */
public abstract class BaseBoundaryFieldPlaybook extends ExpectOnly4XXBaseFieldsPlaybook {

    /**
     * Constructor for initializing common dependencies for fuzzing boundary fields.
     *
     * @param sc The {@link ServiceCaller} used to make service calls
     * @param lr The {@link TestCaseListener} for reporting test case events
     * @param cp The {@link FilesArguments} for file-related arguments
     */
    protected BaseBoundaryFieldPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    public String typeOfDataSentToTheService() {
        return "outside the boundary values";
    }

    @Override
    public List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
        Schema<?> schema = data.getRequestPropertyTypes().get(fuzzedField);

        if (this.fuzzedFieldHasAnAssociatedSchema(schema) && this.isFieldPartOfPayload(fuzzedField, data.getPayload())) {
            logger.debug("Field {} has an associated schema", fuzzedField);
            logger.note("Field [{}] schema is [{}] and type [{}]", fuzzedField, schema.getClass().getSimpleName(), schema.getType());
            Object generatedBoundaryValue = this.getBoundaryValue(schema);
            if (this.isFieldFuzzable(fuzzedField, data) && generatedBoundaryValue != null) {
                logger.debug("Field {} is fuzzable and has boundary value", fuzzedField);
                logger.debug("{} type matching. Start fuzzing...", getSchemaTypesThePlaybookWillApplyTo());
                return Collections.singletonList(FuzzingStrategy.replace().withData(generatedBoundaryValue));
            } else if (!this.hasBoundaryDefined(fuzzedField, data)) {
                logger.debug("Field {} does not have a boundary defined", fuzzedField);
                logger.skip("Boundaries not defined. Will skip fuzzing...");
                return Collections.singletonList(FuzzingStrategy.skip().withData("No LEFT or RIGHT boundary info within the contract!"));
            } else {
                logger.debug("Data type not matching. Skipping fuzzing for {}", fuzzedField);
                logger.skip("Not {}. Will skip fuzzing...", getSchemaTypesThePlaybookWillApplyTo());
            }
        }
        return Collections.singletonList(FuzzingStrategy.skip().withData("Data type not matching " + getSchemaTypesThePlaybookWillApplyTo()));
    }

    /**
     * There are cases when field is part of a different oneOf, anyOf element.
     *
     * @param field   the field being fuzzed
     * @param payload the payload
     * @return true if field is part of the payload, false otherwise
     */
    private boolean isFieldPartOfPayload(String field, String payload) {
        return JsonUtils.isFieldInJson(payload, field);
    }

    /**
     * Checks if the current field is fuzzable by the boundary playbooks. The following cases might skip the current playbook:
     * <ol>
     *     <li>the fuzzed field schema is not fuzzable by the current playbook. Each boundary Playbook provides a list with all applicable schemas.</li>
     *     <li>there is no boundary defined for the current field. This is also specific for each Playbook</li>
     *     <li>the string format is not recognizable. Check {@link InvalidDataFormat} for supported formats</li>
     * </ol>
     *
     * @param fuzzedField the current fuzzed field
     * @param data        the current FuzzingData
     * @return true if the field is fuzzble, false otherwise
     */
    protected boolean isFieldFuzzable(String fuzzedField, PlaybookData data) {
        Schema schema = data.getRequestPropertyTypes().get(fuzzedField);
        return this.isRequestSchemaMatchingPlaybookType(schema) && this.hasBoundaryDefined(fuzzedField, data);
    }

    private boolean fuzzedFieldHasAnAssociatedSchema(Schema schema) {
        return schema != null;
    }

    boolean isRequestSchemaMatchingPlaybookType(Schema schema) {
        return getSchemaTypesThePlaybookWillApplyTo().stream().anyMatch(currentSchemaType -> currentSchemaType.equalsIgnoreCase(schema.getType()));
    }

    /**
     * Override this to provide information about which Schema {@code type} the Playbook is applicable to.
     * For example if the current Schema is of type String, but the Playbook applies to Integer values then the Playbook will get skipped.
     *
     * @return schema type for which the Playbook will apply.
     */
    public abstract List<String> getSchemaTypesThePlaybookWillApplyTo();

    /**
     * The value that will be used for fuzzing.
     *
     * @param schema used to extract boundary information
     * @return a value to be used for fuzzing
     */
    public abstract Object getBoundaryValue(Schema schema);

    /**
     * Override this to provide information about whether the current field has boundaries defined or not. For example a String
     * field without minLength defined is not considered to have a left boundary.
     *
     * @param fuzzedField used to extract boundary information
     * @param data        FuzzingData constructed by dochia
     * @return true if the filed has a boundary defined or false otherwise
     */
    public abstract boolean hasBoundaryDefined(String fuzzedField, PlaybookData data);
}
