package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class OrderNumberGeneratorTest {

    private OrderNumberGenerator orderNumberGenerator;

    @BeforeEach
    void setup() {
        orderNumberGenerator = new OrderNumberGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "order,randomField,true",
            "ordernumber,randomField,true",
            "not,orderNumber,true",
            "not,orderId,true",
            "not,randomField,false"
    })
    void shouldRecognizeOrderNumber(String format, String property, boolean expected) {
        boolean isOrderNumber = orderNumberGenerator.appliesTo(format, property);
        Assertions.assertThat(isOrderNumber).isEqualTo(expected);
    }

    @Test
    void givenAnOrderNumberFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(orderNumberGenerator.getAlmostValidValue()).isEqualTo("ORD-123");
    }

    @Test
    void givenAnOrderNumberFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(orderNumberGenerator.getTotallyWrongValue()).isEqualTo("order");
    }

    @Test
    void givenAnOrderNumberFormatGeneratorStrategy_whenGenerating_thenValidOrderNumberIsReturned() {
        String generated = (String) orderNumberGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated.length()).isGreaterThan(8);
    }
}
