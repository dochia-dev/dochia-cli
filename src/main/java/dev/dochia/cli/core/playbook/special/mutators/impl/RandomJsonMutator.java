package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.playbook.special.mutators.api.BodyMutator;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Sends a random json payloads replacing the entire input json.
 */
@Singleton
public class RandomJsonMutator implements BodyMutator {

    @Override
    public String mutate(String inputJson, String selectedField) {
        List<String> randomPayloads = UnicodeGenerator.getInvalidJsons();

        return randomPayloads.get(CommonUtils.random().nextInt(randomPayloads.size()));
    }

    @Override
    public String description() {
        return "replace body with random invalid jsons";
    }
}