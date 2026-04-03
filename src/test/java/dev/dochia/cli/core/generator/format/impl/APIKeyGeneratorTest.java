package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.util.CommonUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class APIKeyGeneratorTest {

    private APIKeyGenerator apiKeyGenerator;

    @BeforeEach
    void setup() {
        apiKeyGenerator = new APIKeyGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "apikey,randomField,true",
            "APIKEY,randomField,true",
            "not,apiKey,true",
            "not,api_key,true",
            "not,api-key,true",
            "not,myApiKey,true",
            "not,randomField,false",
            "other,other,false"
    })
    void shouldRecognizeAPIKey(String format, String property, boolean expected) {
        assertThat(apiKeyGenerator.appliesTo(format, property)).isEqualTo(expected);
    }

    @Test
    void givenAnAPIKeyFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        assertThat(apiKeyGenerator.getAlmostValidValue()).isEqualTo("sk_live_abc123");
    }

    @Test
    void givenAnAPIKeyFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        assertThat(apiKeyGenerator.getTotallyWrongValue()).isEqualTo("invalid-key");
    }

    @Test
    void shouldReturnMatchingFormats() {
        assertThat(apiKeyGenerator.matchingFormats())
                .containsExactly("apikey", "api-key", "api_key");
    }

    @Test
    void shouldGenerateSimpleAlphanumericKey() {
        // seed 1 → nextInt(3)==0
        CommonUtils.initRandom(1);
        String generated = (String) apiKeyGenerator.generate(null);

        assertThat(generated).isNotNull()
                .hasSize(32)
                .matches("[A-Za-z0-9]{32}");
    }

    @Test
    void shouldGenerateStripeStyleKey() {
        // seed 2 → nextInt(3)==1
        CommonUtils.initRandom(2);
        String generated = (String) apiKeyGenerator.generate(null);

        assertThat(generated).isNotNull()
                .startsWith("sk_live_")
                .hasSize(32);
    }

    @Test
    void shouldGenerateGoogleStyleKey() {
        // seed 3 → nextInt(3)==2
        CommonUtils.initRandom(3);
        String generated = (String) apiKeyGenerator.generate(null);

        assertThat(generated).isNotNull()
                .startsWith("AIza")
                .hasSize(39);
    }

    @Test
    void shouldGenerateNonNullAcrossMultipleSeeds() {
        for (int seed = 0; seed < 20; seed++) {
            CommonUtils.initRandom(seed);
            String generated = (String) apiKeyGenerator.generate(null);
            assertThat(generated).as("seed=%d", seed)
                    .isNotNull()
                    .isNotEmpty()
                    .hasSizeGreaterThan(20);
        }
    }
}
