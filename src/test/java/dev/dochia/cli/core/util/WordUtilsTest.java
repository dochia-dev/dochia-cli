package dev.dochia.cli.core.util;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

@QuarkusTest
class WordUtilsTest {

    @Test
    void shouldReturnAllCombinations() {
        String[] words = new String[]{"pet", "name", "id"};
        Set<String> result = WordUtils.createWordCombinations(words);

        Assertions.assertThat(result).containsOnly("ID", "Id", "NAME-ID", "NAMEID", "NAME_ID", "Name-Id", "NameId",
                "Name_Id", "PET-NAME-ID", "PETNAMEID", "PET_NAME_ID", "Pet-Name-Id", "PetNameId", "Pet_Name_Id",
                "id", "name-Id", "name-id", "nameId", "name_Id", "name_id", "nameid", "pet-Name-Id", "pet-name-id", "petNameId", "pet_Name_Id", "pet_name_id", "petnameid");
    }

    @Test
    void testCollapseEscapedQuotedSegmentsCoverage() {
        // No escaped quotes
        Assertions.assertThat(WordUtils.normalizeErrorMessage("no quotes here")).isEqualTo("no quotes here");

        // One pair of escaped quotes with content
        Assertions.assertThat(WordUtils.normalizeErrorMessage("before \\\"inside\\\" after")).isEqualTo("before \\\"\\\" after");

        // Multiple pairs of escaped quotes
        Assertions.assertThat(WordUtils.normalizeErrorMessage("a \\\"x\\\" b \\\"y\\\" c")).isEqualTo("a \\\"\\\" b \\\"\\\" c");

        // Unclosed escaped quote (no closing pair)
        Assertions.assertThat(WordUtils.normalizeErrorMessage("start \"unfinished")).isEqualTo("start \"unfinished");

        // Escaped quotes at the start
        Assertions.assertThat(WordUtils.normalizeErrorMessage("\\\"foo\\\" bar")).isEqualTo("\\\"\\\" bar");

        // Escaped quotes at the end
        Assertions.assertThat(WordUtils.normalizeErrorMessage("bar \\\"foo\\\"")).isEqualTo("bar \\\"\\\"");

        // Nested or adjacent escaped quotes (should treat as separate pairs)
        Assertions.assertThat(WordUtils.normalizeErrorMessage("a \\\"x\\\"\\\"y\\\" b")).isEqualTo("a \\\"\\\"\\\"\\\" b");

        // Only escaped quotes
        Assertions.assertThat(WordUtils.normalizeErrorMessage("\\\"foo\\\"")).isEqualTo("\\\"\\\"");

        // Empty string
        Assertions.assertThat(WordUtils.normalizeErrorMessage("")).isEmpty();
    }
}
