package dev.dochia.cli.core.util;

import io.quarkus.test.junit.QuarkusTest;
import org.cornutum.regexpgen.RandomGen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
class DochiaRandomTest {

    @BeforeEach
    void setUp() {
        DochiaRandom.initRandom(12345L);
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Should initialize random with specific seed")
        void shouldInitializeRandomWithSpecificSeed() {
            DochiaRandom.initRandom(42L);
            Random random = DochiaRandom.instance();

            assertThat(random).isNotNull();
            assertThat(random.nextInt(100)).isEqualTo(30);
        }

        @Test
        @DisplayName("Should initialize random with zero seed using random seed")
        void shouldInitializeRandomWithZeroSeed() {
            DochiaRandom.initRandom(0L);
            Random random = DochiaRandom.instance();

            assertThat(random).isNotNull();
        }

        @Test
        @DisplayName("Should initialize regexp random generator")
        void shouldInitializeRegexpRandomGen() {
            DochiaRandom.initRandom(42L);
            RandomGen regexpGen = DochiaRandom.regexpRandomGen();

            assertThat(regexpGen).isNotNull();
        }

        @Test
        @DisplayName("Should return same instance after initialization")
        void shouldReturnSameInstance() {
            DochiaRandom.initRandom(42L);
            Random random1 = DochiaRandom.instance();
            Random random2 = DochiaRandom.instance();

            assertThat(random1).isSameAs(random2);
        }
    }

    @Nested
    @DisplayName("Alphanumeric Generation Tests")
    class AlphanumericTests {

        @Test
        @DisplayName("Should generate alphanumeric string of specified length")
        void shouldGenerateAlphanumericString() {
            String result = DochiaRandom.alphanumeric(10);

            assertThat(result).hasSize(10).matches("[a-zA-Z0-9]+");
        }

        @Test
        @DisplayName("Should generate empty string for zero length")
        void shouldGenerateEmptyStringForZeroLength() {
            String result = DochiaRandom.alphanumeric(0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should generate deterministic alphanumeric with same seed")
        void shouldGenerateDeterministicAlphanumeric() {
            DochiaRandom.initRandom(42L);
            String result1 = DochiaRandom.alphanumeric(10);

            DochiaRandom.initRandom(42L);
            String result2 = DochiaRandom.alphanumeric(10);

            assertThat(result1).isEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("Alphabetic Generation Tests")
    class AlphabeticTests {

        @Test
        @DisplayName("Should generate alphabetic string of specified length")
        void shouldGenerateAlphabeticString() {
            String result = DochiaRandom.alphabetic(10);

            assertThat(result).hasSize(10).matches("[a-zA-Z]+");
        }

        @Test
        @DisplayName("Should generate empty string for zero length")
        void shouldGenerateEmptyStringForZeroLength() {
            String result = DochiaRandom.alphabetic(0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should generate deterministic alphabetic with same seed")
        void shouldGenerateDeterministicAlphabetic() {
            DochiaRandom.initRandom(42L);
            String result1 = DochiaRandom.alphabetic(10);

            DochiaRandom.initRandom(42L);
            String result2 = DochiaRandom.alphabetic(10);

            assertThat(result1).isEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("Numeric Generation Tests")
    class NumericTests {

        @Test
        @DisplayName("Should generate numeric string of specified length")
        void shouldGenerateNumericString() {
            String result = DochiaRandom.numeric(10);

            assertThat(result).hasSize(10).matches("[0-9]+");
        }

        @Test
        @DisplayName("Should generate empty string for zero length")
        void shouldGenerateEmptyStringForZeroLength() {
            String result = DochiaRandom.numeric(0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should generate deterministic numeric with same seed")
        void shouldGenerateDeterministicNumeric() {
            DochiaRandom.initRandom(42L);
            String result1 = DochiaRandom.numeric(10);

            DochiaRandom.initRandom(42L);
            String result2 = DochiaRandom.numeric(10);

            assertThat(result1).isEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("Numeric Range Generation Tests")
    class NumericRangeTests {

        @Test
        @DisplayName("Should generate numeric string within range")
        void shouldGenerateNumericStringWithinRange() {
            String result = DochiaRandom.numeric(5, 10);

            assertThat(result).hasSizeBetween(5, 9).matches("[0-9]+");
        }

        @Test
        @DisplayName("Should generate numeric string with equal min and max")
        void shouldGenerateNumericStringWithEqualMinMax() {
            String result = DochiaRandom.numeric(5, 5);

            assertThat(result).hasSize(5).matches("[0-9]+");
        }

        @Test
        @DisplayName("Should throw exception when max is less than min")
        void shouldThrowExceptionWhenMaxLessThanMin() {
            assertThatThrownBy(() -> DochiaRandom.numeric(10, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Start value must be smaller or equal to end value.");
        }

        @Test
        @DisplayName("Should throw exception when min is negative")
        void shouldThrowExceptionWhenMinIsNegative() {
            assertThatThrownBy(() -> DochiaRandom.numeric(-1, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Both range values must be non-negative.");
        }

        @Test
        @DisplayName("Should throw exception when max is negative")
        void shouldThrowExceptionWhenMaxIsNegative() {
            assertThatThrownBy(() -> DochiaRandom.numeric(-5, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Both range values must be non-negative.");
        }

        @Test
        @DisplayName("Should generate zero length string when both are zero")
        void shouldGenerateZeroLengthStringWhenBothZero() {
            String result = DochiaRandom.numeric(0, 0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should generate deterministic numeric range with same seed")
        void shouldGenerateDeterministicNumericRange() {
            DochiaRandom.initRandom(42L);
            String result1 = DochiaRandom.numeric(5, 10);

            DochiaRandom.initRandom(42L);
            String result2 = DochiaRandom.numeric(5, 10);

            assertThat(result1).isEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("ASCII Generation Tests")
    class AsciiTests {

        @Test
        @DisplayName("Should generate ASCII string of specified length")
        void shouldGenerateAsciiString() {
            String result = DochiaRandom.ascii(10);

            assertThat(result).hasSize(10);
            for (char c : result.toCharArray()) {
                assertThat(c).isBetween((char) 32, (char) 126);
            }
        }

        @Test
        @DisplayName("Should generate empty string for zero length")
        void shouldGenerateEmptyStringForZeroLength() {
            String result = DochiaRandom.ascii(0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should generate deterministic ASCII with same seed")
        void shouldGenerateDeterministicAscii() {
            DochiaRandom.initRandom(42L);
            String result1 = DochiaRandom.ascii(10);

            DochiaRandom.initRandom(42L);
            String result2 = DochiaRandom.ascii(10);

            assertThat(result1).isEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("Next Generation Tests")
    class NextTests {

        @Test
        @DisplayName("Should generate random string of specified length")
        void shouldGenerateRandomString() {
            String result = DochiaRandom.next(10);

            assertThat(result).hasSize(10);
        }

        @Test
        @DisplayName("Should generate empty string for zero length")
        void shouldGenerateEmptyStringForZeroLength() {
            String result = DochiaRandom.next(0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should generate deterministic random string with same seed")
        void shouldGenerateDeterministicRandomString() {
            DochiaRandom.initRandom(42L);
            String result1 = DochiaRandom.next(10);

            DochiaRandom.initRandom(42L);
            String result2 = DochiaRandom.next(10);

            assertThat(result1).isEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("Instance Tests")
    class InstanceTests {

        @Test
        @DisplayName("Should return initialized random instance")
        void shouldReturnInitializedRandomInstance() {
            DochiaRandom.initRandom(42L);
            Random random = DochiaRandom.instance();

            assertThat(random).isNotNull();
        }

        @Test
        @DisplayName("Should return regexp random generator instance")
        void shouldReturnRegexpRandomGenInstance() {
            DochiaRandom.initRandom(42L);
            RandomGen regexpGen = DochiaRandom.regexpRandomGen();

            assertThat(regexpGen).isNotNull();
        }
    }
}

