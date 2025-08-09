package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.playbook.special.mutators.api.BodyMutator;
import dev.dochia.cli.core.util.CommonUtils;
import jakarta.inject.Singleton;

/**
 * Sends random single codepoint emojis in the target field.
 */
@Singleton
public class RandomSingleCodepointEmojisMutator implements BodyMutator {
    private static final int BOUND = 15;

    @Override
    public String mutate(String inputJson, String selectedField) {
        String randomEmojis = generateEmojiString();

        return CommonUtils.justReplaceField(inputJson, selectedField, randomEmojis).json();
    }

    private static String generateEmojiString() {
        StringBuilder sb = new StringBuilder();
        int minEmojiCodePoint = 0x1F600; // Start of emoji range
        int maxEmojiCodePoint = 0x1F64F; // End of emoji range
        int emojiRange = maxEmojiCodePoint - minEmojiCodePoint + 1;

        for (int i = 0; i < BOUND; i++) {
            int emojiCodePoint = CommonUtils.random().nextInt(emojiRange) + minEmojiCodePoint;
            sb.append(Character.toChars(emojiCodePoint));
        }
        return sb.toString();
    }

    @Override
    public String description() {
        return "replace field with random single codepoint emojis";
    }
}