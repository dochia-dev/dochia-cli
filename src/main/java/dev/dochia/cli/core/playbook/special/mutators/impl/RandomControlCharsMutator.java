package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.playbook.special.mutators.api.BodyMutator;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import jakarta.inject.Singleton;

/**
 * Sends random control chars in the target field.
 */
@Singleton
public class RandomControlCharsMutator implements BodyMutator {
    private static final int BOUND = 10;

    @Override
    public String mutate(String inputJson, String selectedField) {
        String randomControlChars = UnicodeGenerator.generateRandomUnicodeString(BOUND, Character::isISOControl);

        return CommonUtils.justReplaceField(inputJson, selectedField, randomControlChars).json();
    }

    @Override
    public String description() {
        return "replace field with random control chars";
    }
}