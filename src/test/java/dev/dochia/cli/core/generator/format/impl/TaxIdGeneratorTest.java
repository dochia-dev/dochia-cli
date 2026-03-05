package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class TaxIdGeneratorTest {

    private TaxIdGenerator taxIdGenerator;

    @BeforeEach
    void setup() {
        taxIdGenerator = new TaxIdGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "taxid,randomField,true",
            "ein,randomField,true",
            "not,taxId,true",
            "not,employerIdentificationNumber,true",
            "not,federalTaxId,true",
            "not,randomField,false"
    })
    void shouldRecognizeTaxId(String format, String property, boolean expected) {
        boolean isTaxId = taxIdGenerator.appliesTo(format, property);
        Assertions.assertThat(isTaxId).isEqualTo(expected);
    }

    @Test
    void givenATaxIdFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(taxIdGenerator.getAlmostValidValue()).isEqualTo("12-345678");
    }

    @Test
    void givenATaxIdFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(taxIdGenerator.getTotallyWrongValue()).isEqualTo("ABC-123");
    }

    @Test
    void givenATaxIdFormatGeneratorStrategy_whenGenerating_thenValidTaxIdIsReturned() {
        String generated = (String) taxIdGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull().matches("\\d{2}-\\d{7}");
    }
}
