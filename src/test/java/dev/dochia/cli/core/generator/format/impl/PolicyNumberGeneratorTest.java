package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class PolicyNumberGeneratorTest {

    private PolicyNumberGenerator policyNumberGenerator;

    @BeforeEach
    void setup() {
        policyNumberGenerator = new PolicyNumberGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "policy,randomField,true",
            "policynumber,randomField,true",
            "not,policyNumber,true",
            "not,policyId,true",
            "not,insuranceNumber,true",
            "not,randomField,false"
    })
    void shouldRecognizePolicyNumber(String format, String property, boolean expected) {
        boolean isPolicyNumber = policyNumberGenerator.appliesTo(format, property);
        Assertions.assertThat(isPolicyNumber).isEqualTo(expected);
    }

    @Test
    void givenAPolicyNumberFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(policyNumberGenerator.getAlmostValidValue()).isEqualTo("POL-ABC");
    }

    @Test
    void givenAPolicyNumberFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(policyNumberGenerator.getTotallyWrongValue()).isEqualTo("policy");
    }

    @Test
    void givenAPolicyNumberFormatGeneratorStrategy_whenGenerating_thenValidPolicyNumberIsReturned() {
        String generated = (String) policyNumberGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated.length()).isGreaterThanOrEqualTo(10);
    }
}
