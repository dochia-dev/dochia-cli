package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class InvoiceNumberGeneratorTest {

    private InvoiceNumberGenerator invoiceNumberGenerator;

    @BeforeEach
    void setup() {
        invoiceNumberGenerator = new InvoiceNumberGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "invoice,randomField,true",
            "invoicenumber,randomField,true",
            "not,invoiceNumber,true",
            "not,invoiceId,true",
            "not,randomField,false"
    })
    void shouldRecognizeInvoiceNumber(String format, String property, boolean expected) {
        boolean isInvoiceNumber = invoiceNumberGenerator.appliesTo(format, property);
        Assertions.assertThat(isInvoiceNumber).isEqualTo(expected);
    }

    @Test
    void givenAnInvoiceNumberFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(invoiceNumberGenerator.getAlmostValidValue()).isEqualTo("INV-1234");
    }

    @Test
    void givenAnInvoiceNumberFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(invoiceNumberGenerator.getTotallyWrongValue()).isEqualTo("invoice");
    }

    @Test
    void givenAnInvoiceNumberFormatGeneratorStrategy_whenGenerating_thenValidInvoiceNumberIsReturned() {
        String generated = (String) invoiceNumberGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated.length()).isGreaterThan(4);
    }
}
