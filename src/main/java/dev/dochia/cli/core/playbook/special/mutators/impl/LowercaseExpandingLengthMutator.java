package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.playbook.special.mutators.api.BodyMutator;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.JsonUtils;
import jakarta.inject.Singleton;

@Singleton
public class LowercaseExpandingLengthMutator implements BodyMutator {
    @Override
    public String mutate(String inputJson, String selectedField) {
        int length = String.valueOf(JsonUtils.getVariableFromJson(inputJson, selectedField)).length();
        String generated = CommonUtils.selectRandom(UnicodeGenerator.getLowercaseExpandingLength(), length);
        return CommonUtils.justReplaceField(inputJson, selectedField, generated).json();
    }

    @Override
    public String description() {
        return "replace field with strings that expand length when lowercased";
    }
}
