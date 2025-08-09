package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.playbook.special.mutators.api.BodyMutator;
import dev.dochia.cli.core.util.CommonUtils;
import jakarta.inject.Singleton;

/**
 * Sends random multi codepoint emojis in the target field.
 */
@Singleton
public class RandomMultiCodepointEmojisMutator implements BodyMutator {
    private static final int BOUND = 15;

    @Override
    public String mutate(String inputJson, String selectedField) {
        String randomEmojis = generateEmojiString();

        return CommonUtils.justReplaceField(inputJson, selectedField, randomEmojis).json();
    }

    private static String generateEmojiString() {
        StringBuilder sb = new StringBuilder();
        int minHighSurrogate = 0xD83D; // Start of high surrogate range
        int maxHighSurrogate = 0xD83E; // End of high surrogate range
        int minLowSurrogate = 0xDC00; // Start of low surrogate range
        int maxLowSurrogate = 0xDFFF; // End of low surrogate range

        for (int i = 0; i < BOUND; i++) {
            int highSurrogate = CommonUtils.random().nextInt(maxHighSurrogate - minHighSurrogate + 1) + minHighSurrogate;
            int lowSurrogate = CommonUtils.random().nextInt(maxLowSurrogate - minLowSurrogate + 1) + minLowSurrogate;

            sb.append(Character.toChars(highSurrogate));
            sb.append(Character.toChars(lowSurrogate));
        }
        return sb.toString();
    }

    @Override
    public String description() {
        return "replace field with random multi codepoint emojis";
    }
}