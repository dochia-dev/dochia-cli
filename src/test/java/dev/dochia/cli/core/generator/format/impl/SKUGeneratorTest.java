package dev.dochia.cli.core.generator.format.impl;

import dev.dochia.cli.core.util.CommonUtils;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

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
            "SKU,randomField,true",
            "not,sku,true",
            "not,productCode,true",
            "not,product_code,true",
            "not,itemCode,true",
            "not,item-code,true",
            "not,stockKeepingUnit,true",
            "not,randomField,false",
            "other,other,false"
    })
    void shouldRecognizeSKU(String format, String property, boolean expected) {
        assertThat(skuGenerator.appliesTo(format, property)).isEqualTo(expected);
    }

    @Test
    void givenASKUFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        assertThat(skuGenerator.getAlmostValidValue()).isEqualTo("SKU@123#456");
    }

    @Test
    void givenASKUFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        assertThat(skuGenerator.getTotallyWrongValue()).isEqualTo("sku");
    }

    @Test
    void shouldReturnMatchingFormats() {
        assertThat(skuGenerator.matchingFormats())
                .containsExactly("sku", "product-code", "item-code");
    }

    @Test
    void shouldGenerateCategoryBrandProductFormat() {
        // seed 1 → nextInt(3)==0
        CommonUtils.initRandom(1);
        String generated = (String) skuGenerator.generate(null);

        assertThat(generated).isNotNull()
                .contains("-")
                .hasSizeGreaterThan(5);
    }

    @Test
    void shouldGenerateSimpleNumericWithPrefixFormat() {
        // seed 2 → nextInt(3)==1
        CommonUtils.initRandom(2);
        String generated = (String) skuGenerator.generate(null);

        assertThat(generated).isNotNull()
                .startsWith("SKU-")
                .hasSize(10);
    }

    @Test
    void shouldGenerateAlphanumericFormat() {
        // seed 3 → nextInt(3)==2
        CommonUtils.initRandom(3);
        String generated = (String) skuGenerator.generate(null);

        assertThat(generated).isNotNull()
                .matches("[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}");
    }

    @Test
    void shouldGenerateNonNullAcrossMultipleSeeds() {
        for (int seed = 0; seed < 20; seed++) {
            CommonUtils.initRandom(seed);
            String generated = (String) skuGenerator.generate(null);
            assertThat(generated).as("seed=%d", seed).isNotNull().isNotEmpty();
        }
    }
}
