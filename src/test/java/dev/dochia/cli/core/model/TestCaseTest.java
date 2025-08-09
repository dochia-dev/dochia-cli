package dev.dochia.cli.core.model;


import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@QuarkusTest
class TestCaseTest {

    @Test
    void shouldNotUpdateServer() {
        TestCase testCase = new TestCase();
        testCase.setFullRequestPath("http://localhost:8080/orders");
        testCase.updateServer(null);

        Assertions.assertThat(testCase.getFullRequestPath()).isEqualTo("http://localhost:8080/orders");
    }

    @Test
    void shouldUpdateServer() {
        TestCase testCase = new TestCase();
        testCase.setFullRequestPath("http://localhost:8080/orders");
        testCase.setRequest(HttpRequest.builder().url("http://localhost:8080/orders").build());
        testCase.setServer("http://localhost:8080");
        testCase.updateServer("http://example.com");

        Assertions.assertThat(testCase.getFullRequestPath()).isEqualTo("http://example.com/orders");
        Assertions.assertThat(testCase.getRequest().getUrl()).isEqualTo("http://example.com/orders");
    }

    @ParameterizedTest
    @CsvSource({"skipped,false", "skip_reporting,false", "success,true", "other,true"})
    void shouldReportSkip(String result, boolean skip) {
        TestCase testCase = new TestCase();
        testCase.setResult(result);
        Assertions.assertThat(testCase.isNotSkipped()).isEqualTo(skip);
    }

    @Test
    void shouldSetSkip() {
        TestCase testCase = new TestCase();
        testCase.setResultSkipped();
        Assertions.assertThat(testCase.isNotSkipped()).isFalse();
    }

}
