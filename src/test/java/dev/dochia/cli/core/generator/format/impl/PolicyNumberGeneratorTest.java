package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.util.CommonUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

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
            "POLICY,randomField,true",
            "policynumber,randomField,true",
            "POLICYNUMBER,randomField,true",
            "not,policyNumber,true",
            "not,policy_number,true",
            "not,policyId,true",
            "not,policy-id,true",
            "not,insuranceNumber,true",
            "not,insurance_number,true",
            "not,randomField,false",
            "other,other,false"
    })
    void shouldRecognizePolicyNumber(String format, String property, boolean expected) {
        assertThat(policyNumberGenerator.appliesTo(format, property)).isEqualTo(expected);
    }

    @Test
    void givenAPolicyNumberFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        assertThat(policyNumberGenerator.getAlmostValidValue()).isEqualTo("POL-ABC");
    }

    @Test
    void givenAPolicyNumberFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        assertThat(policyNumberGenerator.getTotallyWrongValue()).isEqualTo("policy");
    }

    @Test
    void shouldReturnMatchingFormats() {
        assertThat(policyNumberGenerator.matchingFormats())
                .containsExactly("policy", "policy-number", "policynumber", "insurance-number");
    }

    @Test
    void shouldGeneratePolPrefixFormat() {
        // seed 1 → nextInt(3)==0
        CommonUtils.initRandom(1);
        String generated = (String) policyNumberGenerator.generate(null);

        assertThat(generated).isNotNull()
                .startsWith("POL-")
                .hasSize(12)
                .matches("POL-\\d{8}");
    }

    @Test
    void shouldGenerateLetterDashDigitFormat() {
        // seed 2 → nextInt(3)==1
        CommonUtils.initRandom(2);
        String generated = (String) policyNumberGenerator.generate(null);

        assertThat(generated).isNotNull()
                .matches("[A-Z]{2}-\\d{4}-\\d{4}-\\d{2}");
    }

    @Test
    void shouldGenerateTwelveDigitFormat() {
        // seed 3 → nextInt(3)==2
        CommonUtils.initRandom(3);
        String generated = (String) policyNumberGenerator.generate(null);

        assertThat(generated).isNotNull()
                .hasSize(12)
                .matches("\\d{12}");
    }

    @Test
    void shouldGenerateNonNullAcrossMultipleSeeds() {
        for (int seed = 0; seed < 20; seed++) {
            CommonUtils.initRandom(seed);
            String generated = (String) policyNumberGenerator.generate(null);
            assertThat(generated).as("seed=%d", seed)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSizeGreaterThanOrEqualTo(10);
        }
    }
}
