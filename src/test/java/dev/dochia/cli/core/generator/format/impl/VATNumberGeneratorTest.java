package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class VATNumberGeneratorTest {

    private VATNumberGenerator vatNumberGenerator;

    @BeforeEach
    void setup() {
        vatNumberGenerator = new VATNumberGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "vat,randomField,true",
            "vatnumber,randomField,true",
            "not,vatNumber,true",
            "not,taxNumber,true",
            "not,randomField,false"
    })
    void shouldRecognizeVATNumber(String format, String property, boolean expected) {
        boolean isVATNumber = vatNumberGenerator.appliesTo(format, property);
        Assertions.assertThat(isVATNumber).isEqualTo(expected);
    }

    @Test
    void givenAVATNumberFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(vatNumberGenerator.getAlmostValidValue()).isEqualTo("XX123456789");
    }

    @Test
    void givenAVATNumberFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(vatNumberGenerator.getTotallyWrongValue()).isEqualTo("VAT123");
    }

    @Test
    void givenAVATNumberFormatGeneratorStrategy_whenGenerating_thenValidVATNumberIsReturned() {
        String generated = (String) vatNumberGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull().matches("[A-Z]{2}\\d{8,10}");
    }
}
