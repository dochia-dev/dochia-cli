package dev.dochia.cli.core.model;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TestCaseSummaryTest {

    @Test
    void givenTwoTestCaseSummaryInstancesWithTheDifferentStringParts_whenComparingThem_thenTheyAreNotEqual() {
        TestCase testCase1 = new TestCase();
        TestCase testCase2 = new TestCase();
        testCase1.setTestId("ID1");
        testCase2.setTestId("JD1");

        testCase1.setResponse(HttpResponse.empty());
        testCase1.setRequest(HttpRequest.builder().httpMethod("POST").build());
        TestCaseSummary summary1 = TestCaseSummary.fromTestCase(testCase1);
        TestCaseSummary summary2 = TestCaseSummary.fromTestCase(testCase2);

        Assertions.assertThat(summary1).isNotEqualByComparingTo(summary2);
    }

    @Test
    void givenTwoTestCaseSummaryInstancesWithTheSameDetails_whenComparingThem_thenTheyAreEqual() {
        TestCase testCase1 = new TestCase();
        testCase1.setTestId("ID1");
        testCase1.setResponse(HttpResponse.empty());
        testCase1.setRequest(HttpRequest.builder().httpMethod("POST").build());
        TestCaseSummary summary1 = TestCaseSummary.fromTestCase(testCase1);
        TestCaseSummary summary2 = TestCaseSummary.fromTestCase(testCase1);

        Assertions.assertThat(summary1).isEqualByComparingTo(summary2);
    }

    @Test
    void givenTwoTestCaseSummaryInstancesWithTheDifferentDetails_whenComparingThem_thenTheyAreNotEqual() {
        TestCase testCase1 = new TestCase();
        TestCase testCase2 = new TestCase();
        testCase2.setTestId("ID2");
        testCase1.setTestId("ID1");

        testCase1.setResponse(HttpResponse.empty());
        testCase1.setRequest(HttpRequest.builder().httpMethod("POST").build());

        testCase2.setResponse(HttpResponse.empty());
        testCase2.setRequest(HttpRequest.builder().httpMethod("POST").build());
        TestCaseSummary summary1 = TestCaseSummary.fromTestCase(testCase1);
        TestCaseSummary summary2 = TestCaseSummary.fromTestCase(testCase2);

        Assertions.assertThat(summary1).isNotEqualByComparingTo(summary2);
    }

    @Test
    void givenATestCase_whenGettingToString_thenTheScenarioDetailsAreReturned() {
        TestCase testCase = new TestCase();
        testCase.setScenario("Scenario");

        Assertions.assertThat(testCase).hasToString("TestCase(scenario=Scenario)");
    }
}
