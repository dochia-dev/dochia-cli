package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.util.CommonUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

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
            "ORDER,randomField,true",
            "ordernumber,randomField,true",
            "ORDERNUMBER,randomField,true",
            "not,orderNumber,true",
            "not,order_number,true",
            "not,orderId,true",
            "not,order-id,true",
            "not,randomField,false",
            "other,other,false"
    })
    void shouldRecognizeOrderNumber(String format, String property, boolean expected) {
        assertThat(orderNumberGenerator.appliesTo(format, property)).isEqualTo(expected);
    }

    @Test
    void givenAnOrderNumberFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        assertThat(orderNumberGenerator.getAlmostValidValue()).isEqualTo("ORD-123");
    }

    @Test
    void givenAnOrderNumberFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        assertThat(orderNumberGenerator.getTotallyWrongValue()).isEqualTo("order");
    }

    @Test
    void shouldReturnMatchingFormats() {
        assertThat(orderNumberGenerator.matchingFormats())
                .containsExactly("order", "order-number", "ordernumber");
    }

    @Test
    void shouldGenerateOrdPrefixFormat() {
        // seed 1 → nextInt(3)==0
        CommonUtils.initRandom(1);
        String generated = (String) orderNumberGenerator.generate(null);

        assertThat(generated).isNotNull()
                .startsWith("ORD-")
                .hasSizeGreaterThan(8);
    }

    @Test
    void shouldGenerateAmazonStyleFormat() {
        // seed 2 → nextInt(3)==1
        CommonUtils.initRandom(2);
        String generated = (String) orderNumberGenerator.generate(null);

        assertThat(generated).isNotNull()
                .matches("\\d{3}-\\d{7}-\\d{7}");
    }

    @Test
    void shouldGenerateAlphanumericFormat() {
        // seed 3 → nextInt(3)==2
        CommonUtils.initRandom(3);
        String generated = (String) orderNumberGenerator.generate(null);

        assertThat(generated).isNotNull()
                .hasSize(12)
                .matches("[A-Z0-9]{12}");
    }

    @Test
    void shouldGenerateNonNullAcrossMultipleSeeds() {
        for (int seed = 0; seed < 20; seed++) {
            CommonUtils.initRandom(seed);
            String generated = (String) orderNumberGenerator.generate(null);
            assertThat(generated).as("seed=%d", seed).isNotNull().isNotEmpty();
        }
    }
}
