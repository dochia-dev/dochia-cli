package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.playbook.field.base.BaseReplaceFieldsPlaybook;
import dev.dochia.cli.core.generator.simple.UnicodeGenerator;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.util.DochiaModelUtils;
import dev.dochia.cli.core.util.JsonUtils;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Replaces characters inside enum values with Unicode homoglyphs
 * (e.g., LATIN 'A' → GREEK ALPHA) to expose Trojan-Source-style
 * validation or authorisation bypasses.
 */
@FieldPlaybook
@Singleton
public class HomoglyphEnumFieldsPlaybook extends BaseReplaceFieldsPlaybook {

    /**
     * Constructor for initializing common dependencies for fuzzing fields.
     *
     * @param ce the executor
     */
    public HomoglyphEnumFieldsPlaybook(FieldsIteratorExecutor ce) {
        super(ce);
    }

    @Override
    public BaseReplaceFieldsContext getContext(PlaybookData data) {
        BiFunction<Schema<?>, String, List<Object>> producer = (schema, field) -> {
            String original = String.valueOf(JsonUtils.getVariableFromJson(data.getPayload(), field));
            List<Object> results = new ArrayList<>();

            for (int idx = 0; idx < original.length(); idx++) {
                char ch = original.charAt(idx);
                if (UnicodeGenerator.getHomoglyphs().containsKey(ch)) {
                    char homo = UnicodeGenerator.getHomoglyphs().get(ch);
                    String mutated = original.substring(0, idx) + homo + original.substring(idx + 1);
                    if (!mutated.equals(original)) {
                        results.add(mutated);
                    }
                }
            }
            return results.isEmpty() ? List.of(original) : results;
        };

        return BaseReplaceFieldsContext.builder()
                .replaceWhat("enum value")
                .replaceWith("homoglyph-altered value")
                .skipMessage("Playbook only runs for string enums")
                .fieldFilter(field -> {
                    Schema<?> schema = data.getRequestPropertyTypes().get(field);
                    return DochiaModelUtils.isEnumSchema(schema) && JsonUtils.isFieldInJson(data.getPayload(), field);
                })
                .fuzzValueProducer(producer)
                .build();
    }
}