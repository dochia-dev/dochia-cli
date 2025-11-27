package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class OAuth2TokenGeneratorTest {

    private OAuth2TokenGenerator oauth2TokenGenerator;

    @BeforeEach
    void setup() {
        oauth2TokenGenerator = new OAuth2TokenGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "oauth2,randomField,true",
            "bearer,randomField,true",
            "not,oauthToken,true",
            "not,bearerToken,true",
            "not,accessToken,true",
            "not,refreshToken,true",
            "not,randomField,false"
    })
    void shouldRecognizeOAuth2Token(String format, String property, boolean expected) {
        boolean isOAuth2Token = oauth2TokenGenerator.appliesTo(format, property);
        Assertions.assertThat(isOAuth2Token).isEqualTo(expected);
    }

    @Test
    void givenAnOAuth2TokenFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(oauth2TokenGenerator.getAlmostValidValue()).isEqualTo("ya29.short");
    }

    @Test
    void givenAnOAuth2TokenFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(oauth2TokenGenerator.getTotallyWrongValue()).isEqualTo("invalid token");
    }

    @Test
    void givenAnOAuth2TokenFormatGeneratorStrategy_whenGenerating_thenValidTokenIsReturned() {
        String generated = (String) oauth2TokenGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated.length()).isGreaterThanOrEqualTo(40);
    }
}
