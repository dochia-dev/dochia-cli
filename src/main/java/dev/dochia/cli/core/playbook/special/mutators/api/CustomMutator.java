package dev.dochia.cli.core.playbook.special.mutators.api;

import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.JsonUtils;

/**
 * Executes mutator logic from a custom mutator file.
 *
 * <p>
 * Custom mutators are parsed as {@code CustomMutatorConfig} and
 * then transformed into {@code CustomMutator} instances.
 * </p>
 */
public class CustomMutator implements BodyMutator {
    private final CustomMutatorConfig customMutatorConfig;

    public CustomMutator(CustomMutatorConfig customMutatorConfig) {
        this.customMutatorConfig = customMutatorConfig;
    }

    @Override
    public String mutate(String inputJson, String selectedField) {
        Object existingValue = JsonUtils.getVariableFromJson(inputJson, selectedField);

        Object valueToFuzz = CommonUtils.selectRandom(customMutatorConfig.values());

        return switch (customMutatorConfig.type()) {
            case TRAIL -> {
                String valueToReplace = String.valueOf(existingValue) + valueToFuzz;
                yield CommonUtils.justReplaceField(inputJson, selectedField, valueToReplace).json();
            }
            case INSERT -> {
                String valueToReplace = CommonUtils.insertInTheMiddle(String.valueOf(existingValue), String.valueOf(valueToFuzz), true);
                yield CommonUtils.justReplaceField(inputJson, selectedField, valueToReplace).json();
            }
            case PREFIX -> {
                String valueToReplace = valueToFuzz + String.valueOf(existingValue);
                yield CommonUtils.justReplaceField(inputJson, selectedField, valueToReplace).json();
            }
            case REPLACE -> CommonUtils.justReplaceField(inputJson, selectedField, valueToFuzz).json();
            case REPLACE_BODY -> String.valueOf(valueToFuzz);
            case IN_BODY -> {
                char firstChar = inputJson.charAt(0);
                yield firstChar + String.valueOf(valueToFuzz) + "," + inputJson.substring(1);
            }
        };
    }

    @Override
    public String description() {
        return customMutatorConfig.name();
    }
}
