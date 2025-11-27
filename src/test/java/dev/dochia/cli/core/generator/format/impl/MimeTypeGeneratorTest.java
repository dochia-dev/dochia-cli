package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class MimeTypeGeneratorTest {

    private MimeTypeGenerator mimeTypeGenerator;

    @BeforeEach
    void setup() {
        mimeTypeGenerator = new MimeTypeGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "mimetype,randomField,true",
            "mime,randomField,true",
            "not,mimeType,true",
            "not,mediaType,true",
            "not,contentMime,true",
            "not,randomField,false"
    })
    void shouldRecognizeMimeType(String format, String property, boolean expected) {
        boolean isMimeType = mimeTypeGenerator.appliesTo(format, property);
        Assertions.assertThat(isMimeType).isEqualTo(expected);
    }

    @Test
    void givenAMimeTypeFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(mimeTypeGenerator.getAlmostValidValue()).isEqualTo("application/");
    }

    @Test
    void givenAMimeTypeFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(mimeTypeGenerator.getTotallyWrongValue()).isEqualTo("not-a-mime-type");
    }

    @Test
    void givenAMimeTypeFormatGeneratorStrategy_whenGenerating_thenValidMimeTypeIsReturned() {
        String generated = (String) mimeTypeGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated).contains("/");
    }
}
