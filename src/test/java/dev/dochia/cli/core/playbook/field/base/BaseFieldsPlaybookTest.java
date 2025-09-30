package dev.dochia.cli.core.playbook.field.base;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

@QuarkusTest
class BaseFieldsPlaybookTest {

    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;

    private BaseFieldsPlaybook baseFieldsPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(HttpResponse.builder().responseCode(200).body("{}").build());
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void givenAFieldWithAReplaceFuzzingStrategyWithANonPrimitiveField_whenTheFieldIsFuzzedAndNoExceptionOccurs_thenTestsAreSkipped() {
        baseFieldsPlaybook = new MyBaseFieldsPlaybook(serviceCaller, testCaseListener, filesArguments, "");
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Set<String> fields = Collections.singleton("field");
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(fields);
        Mockito.when(data.getPayload()).thenReturn("{}");

        baseFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener).skipTest(Mockito.any(), Mockito.eq("field could not be fuzzed. Possible reasons: field is not a primitive, is a discriminator, is passed as refData or is not matching the Playbook schemas"));
    }

    @Test
    void shouldSkipPlaybookWhenSkipStrategy() {
        baseFieldsPlaybook = new MyBaseFieldsPlaybook(serviceCaller, testCaseListener, filesArguments, "");
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Set<String> fields = Collections.singleton("field");
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(fields);
        Mockito.when(data.getPayload()).thenReturn("{}");
        testCaseListener.createAndExecuteTest(Mockito.mock(PrettyLogger.class), Mockito.mock(TestCasePlaybook.class), () -> baseFieldsPlaybook.process(data, "field", FuzzingStrategy.skip().withData("Skipping test")), data);
        Mockito.verify(testCaseListener).skipTest(Mockito.any(), Mockito.eq("Skipping test"));
    }

    @Test
    void givenAFieldWithASkipFuzzingStrategy_whenTheFieldIsFuzzedAndNoExceptionOccurs_thenTestIsNotRun() {
        baseFieldsPlaybook = new MyBaseFieldsSkipPlaybook(serviceCaller, testCaseListener, filesArguments);
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Set<String> fields = Collections.singleton("field");
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(fields);

        baseFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void givenAJSonPrimitiveFieldWithAReplaceFuzzingStrategy_whenTheFieldIsFuzzedAndNoExceptionOccurs_thenTestsAreExecuted() {
        PlaybookData data = createFuzzingData();

        baseFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any(), Mockito.eq(true), Mockito.eq(true));
    }

    @NotNull
    private PlaybookData createFuzzingData() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Set<String> fields = Collections.singleton("field");
        Map<String, Schema> schemaMap = new HashMap<>();
        schemaMap.put("field", new StringSchema());
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(fields);
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(schemaMap);
        Mockito.when(data.getPayload()).thenReturn("{\"field\": 2}");

        baseFieldsPlaybook = new MyBaseFieldsPlaybook(serviceCaller, testCaseListener, filesArguments, "");

        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any());
        return data;
    }

    @Test
    void shouldNotRunWhenNoFields() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Collections.emptySet());
        baseFieldsPlaybook = new MyBaseFieldsPlaybook(serviceCaller, testCaseListener, filesArguments, "");

        baseFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldSkipWhenFuzzingNotPossibleFromPlaybook() {
        PlaybookData data = createFuzzingData();
        BaseFieldsPlaybook spyPlaybook = Mockito.spy(baseFieldsPlaybook);
        Mockito.when(spyPlaybook.isPlaybookApplicable(Mockito.eq(data), Mockito.anyString())).thenReturn(false);
        spyPlaybook.run(data);
        Mockito.verify(testCaseListener).skipTest(Mockito.any(), Mockito.eq("field could not be fuzzed. Possible reasons: field is not a primitive, is a discriminator, is passed as refData or is not matching the Playbook schemas"));
    }

    @Test
    void shouldSkipWhenSkippedField() {
        PlaybookData data = createFuzzingData();
        BaseFieldsPlaybook spyPlaybook = Mockito.spy(baseFieldsPlaybook);
        Mockito.when(spyPlaybook.skipForFields()).thenReturn(List.of("field"));
        spyPlaybook.run(data);
        Mockito.verify(testCaseListener).skipTest(Mockito.any(), Mockito.eq("field could not be fuzzed. Possible reasons: field is not a primitive, is a discriminator, is passed as refData or is not matching the Playbook schemas"));
    }

    @ParameterizedTest
    @CsvSource(value = {"null,[a-z]+,200", "dochia,[a-z]+,200", "DOCHIA,[a-z]+,400"}, nullValues = "null")
    void shouldExpectDifferentCodesBasedOnFuzzedFieldMatchingPattern(String fuzzedValue, String pattern, String responseCode) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Set<String> fields = Collections.singleton("field");
        Map<String, Schema> schemaMap = new HashMap<>();
        StringSchema schema = new StringSchema();
        schema.setPattern(pattern);
        schemaMap.put("field", schema);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(fields);
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(schemaMap);
        Mockito.when(data.getPayload()).thenReturn("{\"field\": 2}");

        baseFieldsPlaybook = new MyBaseFieldsPlaybook(serviceCaller, testCaseListener, filesArguments, fuzzedValue);
        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any());

        baseFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.eq(ResponseCodeFamilyPredefined.from(responseCode)), Mockito.eq(true), Mockito.eq(true));
    }

    static class MyBaseFieldsPlaybook extends BaseFieldsPlaybook {
        private final String fuzzedValue;

        public MyBaseFieldsPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp, String fuzzedValue) {
            super(sc, lr, cp);
            this.fuzzedValue = fuzzedValue;
        }

        @Override
        protected String typeOfDataSentToTheService() {
            return "test data";
        }

        @Override
        protected ResponseCodeFamily getExpectedHttpCodeWhenRequiredFieldsAreFuzzed() {
            return ResponseCodeFamilyPredefined.FOURXX;
        }

        @Override
        protected ResponseCodeFamily getExpectedHttpCodeWhenOptionalFieldsAreFuzzed() {
            return ResponseCodeFamilyPredefined.TWOXX;
        }

        @Override
        protected ResponseCodeFamily getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern() {
            return ResponseCodeFamilyPredefined.FOURXX;
        }

        @Override
        protected List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
            return Collections.singletonList(FuzzingStrategy.replace().withData(fuzzedValue));
        }

        @Override
        public String description() {
            return "cool description";
        }
    }

    static class MyBaseFieldsSkipPlaybook extends BaseFieldsPlaybook {

        public MyBaseFieldsSkipPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
            super(sc, lr, cp);
        }

        @Override
        protected String typeOfDataSentToTheService() {
            return "test data";
        }

        @Override
        protected ResponseCodeFamily getExpectedHttpCodeWhenRequiredFieldsAreFuzzed() {
            return null;
        }

        @Override
        protected ResponseCodeFamily getExpectedHttpCodeWhenOptionalFieldsAreFuzzed() {
            return null;
        }

        @Override
        protected ResponseCodeFamily getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern() {
            return null;
        }

        @Override
        protected List<FuzzingStrategy> getFieldFuzzingStrategy(PlaybookData data, String fuzzedField) {
            return Collections.singletonList(FuzzingStrategy.skip().withData(""));
        }

        @Override
        public String description() {
            return "cool description";
        }
    }


}
