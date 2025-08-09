package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.playbook.special.mutators.api.BodyMutator;
import dev.dochia.cli.core.util.JsonUtils;
import jakarta.inject.Singleton;

/**
 * Removes the field from the json body.
 */
@Singleton
public class RemoveFieldMutator implements BodyMutator {

    @Override
    public String mutate(String inputJson, String selectedField) {
        return JsonUtils.deleteNode(inputJson, selectedField);
    }

    @Override
    public String description() {
        return "remove field from the body";
    }
}
