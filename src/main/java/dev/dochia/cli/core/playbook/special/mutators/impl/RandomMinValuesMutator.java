package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.playbook.special.mutators.api.BodyMutator;
import dev.dochia.cli.core.util.CommonUtils;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Sends a random number min values in the target field.
 */
@Singleton
public class RandomMinValuesMutator implements BodyMutator {
    private static final List<Object> MIN_VALUES = List.of(Float.MIN_VALUE, Integer.MIN_VALUE, Long.MIN_VALUE,
            Double.MIN_VALUE, Byte.MIN_VALUE, Short.MIN_VALUE, Float.MIN_NORMAL, Double.MIN_NORMAL, -Float.MIN_VALUE,
            -Float.MIN_NORMAL, -Double.MIN_VALUE, -Double.MIN_NORMAL);

    @Override
    public String mutate(String inputJson, String selectedField) {
        Object toReplaceWith = MIN_VALUES.get(CommonUtils.random().nextInt(MIN_VALUES.size()));
        return CommonUtils.justReplaceField(inputJson, selectedField, toReplaceWith).json();
    }

    @Override
    public String description() {
        return "replace field with random min values for int, long, float or double";
    }
}