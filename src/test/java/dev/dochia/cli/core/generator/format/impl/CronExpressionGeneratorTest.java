package dev.dochia.cli.core.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class CronExpressionGeneratorTest {

    private CronExpressionGenerator cronExpressionGenerator;

    @BeforeEach
    void setup() {
        cronExpressionGenerator = new CronExpressionGenerator();
    }

    @ParameterizedTest
    @CsvSource({
            "cron,randomField,true",
            "not,cronExpression,true",
            "not,scheduleExpression,true",
            "not,randomField,false"
    })
    void shouldRecognizeCronExpression(String format, String property, boolean expected) {
        boolean isCronExpression = cronExpressionGenerator.appliesTo(format, property);
        Assertions.assertThat(isCronExpression).isEqualTo(expected);
    }

    @Test
    void givenACronExpressionFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(cronExpressionGenerator.getAlmostValidValue()).isEqualTo("60 0 * * *");
    }

    @Test
    void givenACronExpressionFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        Assertions.assertThat(cronExpressionGenerator.getTotallyWrongValue()).isEqualTo("not a cron");
    }

    @Test
    void givenACronExpressionFormatGeneratorStrategy_whenGenerating_thenValidCronExpressionIsReturned() {
        String generated = (String) cronExpressionGenerator.generate(null);
        Assertions.assertThat(generated).isNotNull();
        Assertions.assertThat(generated.split(" ")).hasSize(5);
    }
}
