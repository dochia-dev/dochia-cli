package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class MedicalRecordNumberGeneratorTest {

    private MedicalRecordNumberGenerator medicalRecordNumberGenerator;

    @BeforeEach
    void setup() {
        medicalRecordNumberGenerator = new MedicalRecordNumberGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "mrn,randomField,true",
            "medicalrecordnumber,randomField,true",
            "not,mrn,true",
            "not,medicalRecord,true",
            "not,patientId,true",
            "not,randomField,false"
    })
    void shouldRecognizeMedicalRecordNumber(String format, String property, boolean expected) {
        boolean isMRN = medicalRecordNumberGenerator.appliesTo(format, property);
        Assertions.assertThat(isMRN).isEqualTo(expected);
    }

    @Test
    void givenAMedicalRecordNumberFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(medicalRecordNumberGenerator.getAlmostValidValue()).isEqualTo("MRN-123");
    }

    @Test
    void givenAMedicalRecordNumberFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(medicalRecordNumberGenerator.getTotallyWrongValue()).isEqualTo("patient");
    }

    @Test
    void givenAMedicalRecordNumberFormatGeneratorStrategy_whenGenerating_thenValidMRNIsReturned() {
        String generated = (String) medicalRecordNumberGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated.length()).isGreaterThanOrEqualTo(8);
    }
}
