package dev.dochia.cli.core.util;


import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

@QuarkusTest
class DochiaFakerTest {

    private DochiaFaker dochiaFaker;

    @BeforeEach
    void setUp() {
        DochiaRandom.initRandom(0);
        dochiaFaker = new DochiaFaker();
    }

    @Nested
    @DisplayName("BookFaker Tests")
    class BookFakerTests {

        @Test
        void shouldReturnBookTitle() {
            String title = dochiaFaker.book().title();

            Assertions.assertThat(title).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnSameInstanceOnMultipleCalls() {
            Assertions.assertThat(dochiaFaker.book()).isSameAs(dochiaFaker.book());
        }
    }

    @Nested
    @DisplayName("ColorFaker Tests")
    class ColorFakerTests {

        @Test
        void shouldReturnColorName() {
            String color = dochiaFaker.color().name();

            Assertions.assertThat(color).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnSameInstanceOnMultipleCalls() {
            Assertions.assertThat(dochiaFaker.color()).isSameAs(dochiaFaker.color());
        }
    }

    @Nested
    @DisplayName("AncientFaker Tests")
    class AncientFakerTests {

        @Test
        void shouldReturnGodName() {
            String god = dochiaFaker.ancient().god();

            Assertions.assertThat(god).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnPrimordialName() {
            String primordial = dochiaFaker.ancient().primordial();

            Assertions.assertThat(primordial).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnTitanName() {
            String titan = dochiaFaker.ancient().titan();

            Assertions.assertThat(titan).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnHeroName() {
            String hero = dochiaFaker.ancient().hero();

            Assertions.assertThat(hero).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnSameInstanceOnMultipleCalls() {
            Assertions.assertThat(dochiaFaker.ancient()).isSameAs(dochiaFaker.ancient());
        }
    }

    @Nested
    @DisplayName("AddressFaker Tests")
    class AddressFakerTests {

        @Test
        void shouldReturnCity() {
            String city = dochiaFaker.address().city();

            Assertions.assertThat(city).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnCountry() {
            String country = dochiaFaker.address().country();

            Assertions.assertThat(country).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnCountryCode() {
            String countryCode = dochiaFaker.address().countryCode();

            Assertions.assertThat(countryCode).isNotNull().isNotEmpty().hasSizeBetween(2, 3);
        }

        @Test
        void shouldReturnState() {
            String state = dochiaFaker.address().state();

            Assertions.assertThat(state).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnStateAbbr() {
            String stateAbbr = dochiaFaker.address().stateAbbr();

            Assertions.assertThat(stateAbbr).isNotNull().hasSize(2);
        }

        @Test
        void shouldReturnZipCode() {
            String zipCode = dochiaFaker.address().zipCode();

            Assertions.assertThat(zipCode).isNotNull().isNotEmpty().matches("\\d{5}(-\\d{4})?");
        }

        @Test
        void shouldReturnFullAddress() {
            String fullAddress = dochiaFaker.address().fullAddress();

            Assertions.assertThat(fullAddress).isNotNull().isNotEmpty().contains(",");
        }

        @Test
        void shouldReturnStreetAddress() {
            String streetAddress = dochiaFaker.address().streetAddress();

            Assertions.assertThat(streetAddress).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnSameInstanceOnMultipleCalls() {
            Assertions.assertThat(dochiaFaker.address()).isSameAs(dochiaFaker.address());
        }
    }

    @Nested
    @DisplayName("CompanyFaker Tests")
    class CompanyFakerTests {

        @Test
        void shouldReturnCompanyName() {
            String companyName = dochiaFaker.company().name();

            Assertions.assertThat(companyName).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnIndustry() {
            String industry = dochiaFaker.company().industry();

            Assertions.assertThat(industry).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnProfession() {
            String profession = dochiaFaker.company().profession();

            Assertions.assertThat(profession).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnSameInstanceOnMultipleCalls() {
            Assertions.assertThat(dochiaFaker.company()).isSameAs(dochiaFaker.company());
        }
    }

    @Nested
    @DisplayName("NameFaker Tests")
    class NameFakerTests {

        @Test
        void shouldReturnFirstName() {
            String firstName = dochiaFaker.name().firstName();

            Assertions.assertThat(firstName).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnLastName() {
            String lastName = dochiaFaker.name().lastName();

            Assertions.assertThat(lastName).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnPrefix() {
            String prefix = dochiaFaker.name().prefix();

            Assertions.assertThat(prefix).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnSuffix() {
            String suffix = dochiaFaker.name().suffix();

            Assertions.assertThat(suffix).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnFullName() {
            String fullName = dochiaFaker.name().fullName();

            Assertions.assertThat(fullName).isNotNull().isNotEmpty().contains(" ");
        }

        @Test
        void shouldReturnName() {
            String name = dochiaFaker.name().name();

            Assertions.assertThat(name).isNotNull().isNotEmpty().contains(" ");
        }

        @Test
        void shouldReturnUsername() {
            String username = dochiaFaker.name().username();

            Assertions.assertThat(username).isNotNull().isNotEmpty().contains(".");
        }

        @Test
        void shouldReturnSameInstanceOnMultipleCalls() {
            Assertions.assertThat(dochiaFaker.name()).isSameAs(dochiaFaker.name());
        }
    }

    @Nested
    @DisplayName("ChuckNorrisFaker Tests")
    class ChuckNorrisFakerTests {

        @Test
        void shouldReturnFact() {
            String fact = dochiaFaker.chuckNorris().fact();

            Assertions.assertThat(fact).isNotNull().isNotEmpty();
        }

        @Test
        void shouldReturnSameInstanceOnMultipleCalls() {
            Assertions.assertThat(dochiaFaker.chuckNorris()).isSameAs(dochiaFaker.chuckNorris());
        }
    }

    @Nested
    @DisplayName("DateFaker Tests")
    class DateFakerTests {

        @Test
        void shouldReturnBirthday() {
            LocalDate birthday = dochiaFaker.date().birthday();

            Assertions.assertThat(birthday).isNotNull();
            Assertions.assertThat(birthday.getYear()).isBetween(1950, 2000);
            Assertions.assertThat(birthday.getMonthValue()).isBetween(1, 12);
            Assertions.assertThat(birthday.getDayOfMonth()).isBetween(1, 28);
        }

        @Test
        void shouldReturnSameInstanceOnMultipleCalls() {
            Assertions.assertThat(dochiaFaker.date()).isSameAs(dochiaFaker.date());
        }
    }

    @Nested
    @DisplayName("FinanceFaker Tests")
    class FinanceFakerTests {

        @Test
        void shouldReturnIban() {
            String iban = dochiaFaker.finance().iban();

            Assertions.assertThat(iban).isNotNull().isNotEmpty().hasSizeGreaterThanOrEqualTo(22);
        }

        @Test
        void shouldReturnBic() {
            String bic = dochiaFaker.finance().bic();

            Assertions.assertThat(bic).isNotNull().isNotEmpty().isIn("DEUTDEFF", "COBADEFF", "DRESDEFF", "HYVEDEMM", "GENODEF1");
        }

        @Test
        void shouldReturnSameInstanceOnMultipleCalls() {
            Assertions.assertThat(dochiaFaker.finance()).isSameAs(dochiaFaker.finance());
        }
    }

    @Nested
    @DisplayName("Numerify Tests")
    class NumerifyTests {

        @Test
        void shouldReplaceHashWithDigits() {
            String result = dochiaFaker.numerify("###");

            Assertions.assertThat(result).matches("\\d{3}");
        }

        @Test
        void shouldReplaceHashesInPattern() {
            String result = dochiaFaker.numerify("AB-###-CD");

            Assertions.assertThat(result).matches("AB-\\d{3}-CD");
        }

        @Test
        void shouldHandlePatternWithoutHashes() {
            String result = dochiaFaker.numerify("ABCD");

            Assertions.assertThat(result).isEqualTo("ABCD");
        }

        @Test
        void shouldHandleEmptyPattern() {
            String result = dochiaFaker.numerify("");

            Assertions.assertThat(result).isEmpty();
        }

        @Test
        void shouldHandleMixedPattern() {
            String result = dochiaFaker.numerify("Test-##-##-####");

            Assertions.assertThat(result).matches("Test-\\d{2}-\\d{2}-\\d{4}");
        }
    }
}
