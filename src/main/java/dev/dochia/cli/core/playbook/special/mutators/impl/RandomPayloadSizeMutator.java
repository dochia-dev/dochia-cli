package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.playbook.special.mutators.api.BodyMutator;
import dev.dochia.cli.core.util.CommonUtils;
import jakarta.inject.Singleton;

/**
 * Mutates the JSON payload with a substring of random length from the input JSON.
 */
@Singleton
public class RandomPayloadSizeMutator implements BodyMutator {

    @Override
    public String mutate(String inputJson, String selectedField) {
        int size = CommonUtils.random().nextInt(inputJson.length());
        return CommonUtils.justReplaceField(inputJson, selectedField, inputJson.substring(0, size)).json();
    }

    @Override
    public String description() {
        return "replace the payload with a substring of random length";
    }
}
