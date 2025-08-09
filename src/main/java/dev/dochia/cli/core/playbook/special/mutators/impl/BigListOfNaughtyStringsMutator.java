package dev.dochia.cli.core.playbook.special.mutators.impl;

import dev.dochia.cli.core.exception.DochiaException;
import dev.dochia.cli.core.playbook.special.mutators.api.BodyMutator;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.JsonUtils;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Sends random naughty strings in json fields.
 */
@Singleton
public class BigListOfNaughtyStringsMutator implements BodyMutator {
    private static final List<String> NAUGHTY_STRINGS;

    static {
        try (InputStream inputStream = BigListOfNaughtyStringsMutator.class.getClassLoader().getResourceAsStream("blns.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8))) {

            NAUGHTY_STRINGS = reader.lines()
                    .filter(StringUtils::isNotBlank)
                    .filter(Predicate.not(line -> line.startsWith("#")))
                    .toList();

        } catch (Exception e) {
            throw new DochiaException("Failed to read BLNS resource file", e);
        }
    }

    @Override
    public String mutate(String inputJson, String selectedField) {
        String randomNaughtyString = CommonUtils.selectRandom(NAUGHTY_STRINGS);
        if (CommonUtils.random().nextBoolean()) {
            randomNaughtyString = randomNaughtyString + JsonUtils.getVariableFromJson(inputJson, selectedField);
        }
        return CommonUtils.justReplaceField(inputJson, selectedField, randomNaughtyString).json();
    }

    @Override
    public String description() {
        return "replace field with random naughty strings";
    }
}
