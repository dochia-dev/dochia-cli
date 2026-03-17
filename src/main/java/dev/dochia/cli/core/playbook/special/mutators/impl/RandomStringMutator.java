package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.playbook.special.mutators.api.BodyMutator;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.DochiaRandom;
import jakarta.inject.Singleton;

/**
 * Sends a random unicode value in the target field.
 */
@Singleton
public class RandomStringMutator implements BodyMutator {
    private static final int BOUND = 100;

    @Override
    public String mutate(String inputJson, String selectedField) {
        int size = CommonUtils.random().nextInt(BOUND);
        return CommonUtils.justReplaceField(inputJson, selectedField, DochiaRandom.next(size)).json();
    }

    @Override
    public String description() {
        return "replace field with random unicode strings";
    }
}
