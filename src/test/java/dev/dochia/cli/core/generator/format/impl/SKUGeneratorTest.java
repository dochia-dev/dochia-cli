package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class SKUGeneratorTest {

    private SKUGenerator skuGenerator;

    @BeforeEach
    void setup() {
        skuGenerator = new SKUGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "sku,randomField,true",
            "not,sku,true",
            "not,productCode,true",
            "not,itemCode,true",
            "not,randomField,false"
    })
    void shouldRecognizeSKU(String format, String property, boolean expected) {
        boolean isSKU = skuGenerator.appliesTo(format, property);
        Assertions.assertThat(isSKU).isEqualTo(expected);
    }

    @Test
    void givenASKUFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(skuGenerator.getAlmostValidValue()).isEqualTo("SKU@123#456");
    }

    @Test
    void givenASKUFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(skuGenerator.getTotallyWrongValue()).isEqualTo("sku");
    }

    @Test
    void givenASKUFormatGeneratorStrategy_whenGenerating_thenValidSKUIsReturned() {
        String generated = (String) skuGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated.length()).isGreaterThan(5);
    }
}
