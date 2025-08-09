package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Set;

@QuarkusTest
class NullValuesInFieldsPlaybookTest
 {
    private FilterArguments filterArguments;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private NullValuesInFieldsPlaybook nullValuesInFieldsPlaybook;
    private FilesArguments filesArguments;

    @BeforeEach
    void setup() {
        filterArguments = Mockito.mock(FilterArguments.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        nullValuesInFieldsPlaybook = new NullValuesInFieldsPlaybook(null, testCaseListener, filesArguments, filterArguments);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void shouldNotRunForSkippedFields() {
        Mockito.when(filterArguments.getSkipFields()).thenReturn(Collections.singletonList("id"));
        Assertions.assertThat(nullValuesInFieldsPlaybook.skipForFields()).containsOnly("id");
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("id"));
        Mockito.when(data.getPayload()).thenReturn("{}");
        nullValuesInFieldsPlaybook.run(data);

        Mockito.verify(testCaseListener).skipTest(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldReturnExpectedResultCode2xx() {
        Assertions.assertThat(nullValuesInFieldsPlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(nullValuesInFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveTypeOfData() {
        Assertions.assertThat(nullValuesInFieldsPlaybook.typeOfDataSentToTheService()).isNotBlank();
    }

    @Test
    void shouldReturnSkipStrategy() {
        FuzzingStrategy fuzzingStrategy = nullValuesInFieldsPlaybook.getFieldFuzzingStrategy(null, null).getFirst();
        Assertions.assertThat(fuzzingStrategy.name()).isEqualTo(FuzzingStrategy.replace().name());
        Assertions.assertThat(fuzzingStrategy.getData()).isNull();
    }

    @Test
    void shouldNotRunPlaybookWhenGetButNoQueryParam() {
        PlaybookData data = PlaybookData.builder().method(HttpMethod.GET).queryParams(Set.of("query1")).build();
        Assertions.assertThat(nullValuesInFieldsPlaybook.isPlaybookApplicable(data, "notQuery")).isFalse();
    }

    @Test
    void shouldRunPlaybookWhenGetAndQueryParam() {
        PlaybookData data = PlaybookData.builder().method(HttpMethod.GET).queryParams(Set.of("query1")).build();
        Assertions.assertThat(nullValuesInFieldsPlaybook.isPlaybookApplicable(data, "query1")).isTrue();
    }

    @Test
    void shouldRunPlaybookWhenPost() {
        PlaybookData data = PlaybookData.builder().method(HttpMethod.POST).build();
        Assertions.assertThat(nullValuesInFieldsPlaybook.isPlaybookApplicable(data, "notQuery")).isTrue();
    }
}
