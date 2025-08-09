package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.playbook.special.mutators.api.BodyMutator;
import dev.dochia.cli.core.util.CommonUtils;
import jakarta.inject.Singleton;

/**
 * Sends null value in the target field.
 */
@Singleton
public class NullStringMutator implements BodyMutator {

    @Override
    public String mutate(String inputJson, String selectedField) {
        return CommonUtils.justReplaceField(inputJson, selectedField, null).json();
    }

    @Override
    public String description() {
        return "replace field with null";
    }
}
