package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class MACAddressGeneratorTest {

    private MACAddressGenerator macAddressGenerator;

    @BeforeEach
    void setup() {
        macAddressGenerator = new MACAddressGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "mac,randomField,true",
            "macaddress,randomField,true",
            "not,macAddress,true",
            "not,deviceMac,true",
            "not,stomach,false",
            "not,randomField,false"
    })
    void shouldRecognizeMACAddress(String format, String property, boolean expected) {
        boolean isMACAddress = macAddressGenerator.appliesTo(format, property);
        Assertions.assertThat(isMACAddress).isEqualTo(expected);
    }

    @Test
    void givenAMACAddressFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(macAddressGenerator.getAlmostValidValue()).isEqualTo("00:1G:22:33:44:55");
    }

    @Test
    void givenAMACAddressFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(macAddressGenerator.getTotallyWrongValue()).isEqualTo("not-a-mac");
    }

    @Test
    void givenAMACAddressFormatGeneratorStrategy_whenGenerating_thenValidMACAddressIsReturned() {
        String generated = (String) macAddressGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated).matches("^([0-9A-F]{2}:){5}[0-9A-F]{2}$");
    }
}
