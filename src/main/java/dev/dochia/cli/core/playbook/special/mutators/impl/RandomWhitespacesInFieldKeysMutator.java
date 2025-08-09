package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.playbook.special.mutators.api.BodyMutator;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.util.JsonUtils;
import jakarta.inject.Singleton;

/**
 * Inserts random values in the target field key.
 */
@Singleton
public class RandomWhitespacesInFieldKeysMutator implements BodyMutator {
    private static final int BOUND = 2;

    @Override
    public String mutate(String inputJson, String selectedField) {
        String randomControlChars = UnicodeGenerator.generateRandomUnicodeString(BOUND, Character::isWhitespace);

        return JsonUtils.insertCharactersInFieldKey(inputJson, selectedField, randomControlChars);
    }

    @Override
    public String description() {
        return "insert random whitespaces in field keys";
    }
}