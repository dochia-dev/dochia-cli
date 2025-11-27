package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
            "not,apiKey,true",
            "not,api_key,true",
            "not,randomField,false"
    })
    void shouldRecognizeAPIKey(String format, String property, boolean expected) {
        boolean isAPIKey = apiKeyGenerator.appliesTo(format, property);
        Assertions.assertThat(isAPIKey).isEqualTo(expected);
    }

    @Test
    void givenAnAPIKeyFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(apiKeyGenerator.getAlmostValidValue()).isEqualTo("sk_live_abc123");
    }

    @Test
    void givenAnAPIKeyFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(apiKeyGenerator.getTotallyWrongValue()).isEqualTo("invalid-key");
    }

    @Test
    void givenAnAPIKeyFormatGeneratorStrategy_whenGenerating_thenValidAPIKeyIsReturned() {
        String generated = (String) apiKeyGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated.length()).isGreaterThan(20);
    }
}
