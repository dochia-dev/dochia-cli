package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class SemVerGeneratorTest {

    private SemVerGenerator semVerGenerator;

    @BeforeEach
    void setup() {
        semVerGenerator = new SemVerGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "semver,randomField,true",
            "not,semVer,true",
            "not,semanticVersion,true",
            "not,appVersion,true",
            "not,apiVersion,false",
            "not,randomField,false"
    })
    void shouldRecognizeSemVer(String format, String property, boolean expected) {
        boolean isSemVer = semVerGenerator.appliesTo(format, property);
        Assertions.assertThat(isSemVer).isEqualTo(expected);
    }

    @Test
    void givenASemVerFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(semVerGenerator.getAlmostValidValue()).isEqualTo("1.2");
    }

    @Test
    void givenASemVerFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(semVerGenerator.getTotallyWrongValue()).isEqualTo("v1.x.y");
    }

    @Test
    void givenASemVerFormatGeneratorStrategy_whenGenerating_thenValidSemVerIsReturned() {
        String generated = (String) semVerGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull().containsPattern("\\d+\\.\\d+\\.\\d+");
    }
}
