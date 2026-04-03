package dev.dochia.cli.core.http;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ResponseCodeFamilyDynamicTest {

    @Test
    void shouldReturnJoinedStringForAsString() {
        ResponseCodeFamilyDynamic family = new ResponseCodeFamilyDynamic(List.of("200", "201", "204"));

        assertThat(family.asString()).isEqualTo("200|201|204");
    }

    @Test
    void shouldReturnEmptyStringForAsStringWhenEmpty() {
        ResponseCodeFamilyDynamic family = new ResponseCodeFamilyDynamic(Collections.emptyList());

        assertThat(family.asString()).isEmpty();
    }

    @Test
    void shouldReturnSingleCodeForAsString() {
        ResponseCodeFamilyDynamic family = new ResponseCodeFamilyDynamic(List.of("404"));

        assertThat(family.asString()).isEqualTo("404");
    }

    @Test
    void shouldReturnAllowedResponseCodes() {
        List<String> codes = List.of("200", "201");
        ResponseCodeFamilyDynamic family = new ResponseCodeFamilyDynamic(codes);

        assertThat(family.allowedResponseCodes())
                .containsExactly("200", "201")
                .isUnmodifiable();
    }

    @Test
    void shouldReturnEmptyListForAllowedResponseCodesWhenEmpty() {
        ResponseCodeFamilyDynamic family = new ResponseCodeFamilyDynamic(Collections.emptyList());

        assertThat(family.allowedResponseCodes()).isEmpty();
    }

    @Test
    void shouldBeEqualWhenSameResponseCodes() {
        ResponseCodeFamilyDynamic family1 = new ResponseCodeFamilyDynamic(List.of("200", "201"));
        ResponseCodeFamilyDynamic family2 = new ResponseCodeFamilyDynamic(List.of("200", "201"));

        assertThat(family1).isEqualTo(family2)
                .hasSameHashCodeAs(family2);
    }

    @Test
    void shouldNotBeEqualWhenDifferentResponseCodes() {
        ResponseCodeFamilyDynamic family1 = new ResponseCodeFamilyDynamic(List.of("200", "201"));
        ResponseCodeFamilyDynamic family2 = new ResponseCodeFamilyDynamic(List.of("200", "404"));

        assertThat(family1).isNotEqualTo(family2);
    }

    @Test
    void shouldNotBeEqualToNull() {
        ResponseCodeFamilyDynamic family = new ResponseCodeFamilyDynamic(List.of("200"));

        assertThat(family).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToDifferentType() {
        ResponseCodeFamilyDynamic family = new ResponseCodeFamilyDynamic(List.of("200"));

        assertThat(family).isNotEqualTo("200");
    }

    @Test
    void shouldBeEqualToItself() {
        ResponseCodeFamilyDynamic family = new ResponseCodeFamilyDynamic(List.of("200"));
        Object same = family;

        assertThat(family).isEqualTo(same);
    }

    @Test
    void shouldHaveConsistentHashCode() {
        ResponseCodeFamilyDynamic family = new ResponseCodeFamilyDynamic(List.of("200", "201"));

        assertThat(family).hasSameHashCodeAs(family);
    }

    @Test
    void shouldHaveDifferentHashCodeForDifferentCodes() {
        ResponseCodeFamilyDynamic family1 = new ResponseCodeFamilyDynamic(List.of("200"));
        ResponseCodeFamilyDynamic family2 = new ResponseCodeFamilyDynamic(List.of("500"));

        assertThat(family1.hashCode()).isNotEqualTo(family2.hashCode());
    }

    @Test
    void shouldBeEqualForEmptyLists() {
        ResponseCodeFamilyDynamic family1 = new ResponseCodeFamilyDynamic(Collections.emptyList());
        ResponseCodeFamilyDynamic family2 = new ResponseCodeFamilyDynamic(List.of());

        assertThat(family1).isEqualTo(family2)
                .hasSameHashCodeAs(family2);
    }

    @Test
    void shouldReturnStartingDigitFromFirstCode() {
        ResponseCodeFamilyDynamic family = new ResponseCodeFamilyDynamic(List.of("404", "500"));

        assertThat(family.getStartingDigit()).isEqualTo("4");
    }

    @ParameterizedTest
    @MethodSource("matchesAllowedResponseCodesProvider")
    void shouldMatchAllowedResponseCodes(List<String> codes, String codeToCheck, boolean expected) {
        ResponseCodeFamilyDynamic family = new ResponseCodeFamilyDynamic(codes);

        assertThat(family.matchesAllowedResponseCodes(codeToCheck)).isEqualTo(expected);
    }

    static Stream<Arguments> matchesAllowedResponseCodesProvider() {
        return Stream.of(
                Arguments.of(List.of("200", "201"), "200", true),
                Arguments.of(List.of("200", "201"), "201", true),
                Arguments.of(List.of("200", "201"), "404", false),
                Arguments.of(List.of("2XX"), "200", true),
                Arguments.of(List.of("2XX"), "201", true),
                Arguments.of(List.of("2XX"), "404", false),
                Arguments.of(List.of("4XX", "200"), "404", true),
                Arguments.of(List.of("4XX", "200"), "200", true),
                Arguments.of(List.of("4XX", "200"), "500", false),
                Arguments.of(Collections.emptyList(), "200", false)
        );
    }

    @Test
    void shouldDefensiveCopyInputList() {
        var mutableList = new java.util.ArrayList<>(List.of("200", "201"));
        ResponseCodeFamilyDynamic family = new ResponseCodeFamilyDynamic(mutableList);
        mutableList.add("500");

        assertThat(family.allowedResponseCodes())
                .containsExactly("200", "201")
                .hasSize(2);
    }
}
