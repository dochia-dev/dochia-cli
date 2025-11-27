package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class JWTGeneratorTest {

    private JWTGenerator jwtGenerator;

    @BeforeEach
    void setup() {
        jwtGenerator = new JWTGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "jwt,randomField,true",
            "not,jwtToken,true",
            "not,authToken,true",
            "not,bearerToken,true",
            "not,jsonWebToken,true",
            "not,randomField,false"
    })
    void shouldRecognizeJWT(String format, String property, boolean expected) {
        boolean isJWT = jwtGenerator.appliesTo(format, property);
        Assertions.assertThat(isJWT).isEqualTo(expected);
    }

    @Test
    void givenAJWTFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(jwtGenerator.getAlmostValidValue())
                .isEqualTo("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");
    }

    @Test
    void givenAJWTFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(jwtGenerator.getTotallyWrongValue()).isEqualTo("not.a.valid.jwt.token");
    }

    @Test
    void givenAJWTFormatGeneratorStrategy_whenGenerating_thenValidJWTStructureIsReturned() {
        String generated = (String) jwtGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated.split("\\.")).hasSize(3);
    }
}
