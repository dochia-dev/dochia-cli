package dev.dochia.cli.core.playbook.field.base;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkusTest
class BaseBoundaryFieldPlaybookTest {

    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
    }

    @ParameterizedTest
    @CsvSource({"field,replace", "otherField,skip"})
    void shouldReturnProperFuzzingStrategy(String field, String expectedStrategyAsString) {
        FuzzingStrategy expectedStrategy = expectedStrategyAsString.equalsIgnoreCase("replace") ? FuzzingStrategy.replace() : FuzzingStrategy.skip();
        BaseBoundaryFieldPlaybook myBaseBoundaryPlaybook = new MyBaseBoundaryWithBoundariesPlaybook(serviceCaller, testCaseListener, filesArguments);

        PlaybookData data = getMockFuzzingData();

        FuzzingStrategy strategy = myBaseBoundaryPlaybook.getFieldFuzzingStrategy(data, field).getFirst();
        Assertions.assertThat(strategy.name()).isEqualTo(expectedStrategy.name());
    }

    @Test
    void givenABaseBoundaryPlaybookWithNoDefinedBoundary_whenGettingTheFuzzingStrategy_thenTheSkipStrategyIsBeingReturned() {
        BaseBoundaryFieldPlaybook myBaseBoundaryPlaybook = new MyBaseBoundaryWithoutBoundariesPlaybook(serviceCaller, testCaseListener, filesArguments);

        PlaybookData data = getMockFuzzingData();

        FuzzingStrategy strategy = myBaseBoundaryPlaybook.getFieldFuzzingStrategy(data, "field").getFirst();
        Assertions.assertThat(strategy.name()).isEqualTo(FuzzingStrategy.skip().name());
        Assertions.assertThat(strategy.getData().toString()).startsWith("No LEFT or RIGHT boundary");
    }

    @Test
    void givenABaseBoundaryPlaybookWithNoDefinedBoundaryAndIntegerSchema_whenGettingTheFuzzingStrategy_thenTheSkipStrategyIsBeingReturned() {
        BaseBoundaryFieldPlaybook myBaseBoundaryPlaybook = new MyBaseBoundaryWithBoundariesAndIntegerSchemaPlaybook(serviceCaller, testCaseListener, filesArguments);

        PlaybookData data = getMockFuzzingData();

        FuzzingStrategy strategy = myBaseBoundaryPlaybook.getFieldFuzzingStrategy(data, "field").getFirst();
        Assertions.assertThat(strategy.name()).isEqualTo(FuzzingStrategy.skip().name());
        Assertions.assertThat(strategy.getData().toString()).startsWith("Data type not matching [integer]");
    }

    @Test
    void givenABaseBoundaryPlaybookAndAFieldWithNoSchema_whenGettingTheFuzzingStrategy_thenTheSkipStrategyIsBeingReturned() {
        BaseBoundaryFieldPlaybook myBaseBoundaryPlaybook = new MyBaseBoundaryWithBoundariesAndIntegerSchemaPlaybook(serviceCaller, testCaseListener, filesArguments);
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(new HashMap<>());

        FuzzingStrategy strategy = myBaseBoundaryPlaybook.getFieldFuzzingStrategy(data, "field").getFirst();
        Assertions.assertThat(strategy.name()).isEqualTo(FuzzingStrategy.skip().name());
        Assertions.assertThat(strategy.getData().toString()).startsWith("Data type not matching");
        Assertions.assertThat(myBaseBoundaryPlaybook.typeOfDataSentToTheService()).startsWith("outside the boundary values");
    }

    @Test
    void shouldMatchPlaybookTypeWhenSchemaAssignable() {
        BaseBoundaryFieldPlaybook myBaseBoundaryPlaybook = Mockito.mock(MyBaseBoundaryWithBoundariesPlaybook.class);
        Mockito.doCallRealMethod().when(myBaseBoundaryPlaybook).isRequestSchemaMatchingPlaybookType(Mockito.any());
        Mockito.when(myBaseBoundaryPlaybook.getSchemaTypesThePlaybookWillApplyTo()).thenReturn(List.of("number"));

        Assertions.assertThat(myBaseBoundaryPlaybook.isRequestSchemaMatchingPlaybookType(new NumberSchema())).isTrue();
    }

    @Test
    void shouldNotMatchWhenSchemaNotAssignable() {
        BaseBoundaryFieldPlaybook myBaseBoundaryPlaybook = Mockito.mock(MyBaseBoundaryWithBoundariesPlaybook.class);
        Mockito.doCallRealMethod().when(myBaseBoundaryPlaybook).isRequestSchemaMatchingPlaybookType(Mockito.any());
        Mockito.when(myBaseBoundaryPlaybook.getSchemaTypesThePlaybookWillApplyTo()).thenReturn(List.of("number"));

        Assertions.assertThat(myBaseBoundaryPlaybook.isRequestSchemaMatchingPlaybookType(new IntegerSchema())).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {"number,true", "string,false", "null,false"}, nullValues = "null")
    void shouldMatchPlaybookTypeWhenSchemaTypeMatchesPlaybookSchema(String schemaType, boolean matching) {
        BaseBoundaryFieldPlaybook myBaseBoundaryPlaybook = Mockito.mock(MyBaseBoundaryWithBoundariesPlaybook.class);
        Mockito.doCallRealMethod().when(myBaseBoundaryPlaybook).isRequestSchemaMatchingPlaybookType(Mockito.any());
        Mockito.when(myBaseBoundaryPlaybook.getSchemaTypesThePlaybookWillApplyTo()).thenReturn(List.of("number"));
        Schema schema = new Schema();
        schema.setType(schemaType);

        Assertions.assertThat(myBaseBoundaryPlaybook.isRequestSchemaMatchingPlaybookType(schema)).isEqualTo(matching);
    }

    private PlaybookData getMockFuzzingData() {
        Map<String, Schema> schemaMap = new HashMap<>();
        schemaMap.put("field", new StringSchema());
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getPayload()).thenReturn("{\"field\":\"value\"}");
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(schemaMap);
        return data;
    }

    static class MyBaseBoundaryWithBoundariesPlaybook extends BaseBoundaryFieldPlaybook {

        public MyBaseBoundaryWithBoundariesPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
            super(sc, lr, cp);
        }

        @Override
        public List<String> getSchemaTypesThePlaybookWillApplyTo() {
            return List.of("string");
        }

        @Override
        public String getBoundaryValue(Schema schema) {
            return "test";
        }

        @Override
        public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
            return true;
        }

        @Override
        public String description() {
            return "simple description";
        }
    }

    static class MyBaseBoundaryWithBoundariesButNoBoundaryValuePlaybook extends BaseBoundaryFieldPlaybook {

        public MyBaseBoundaryWithBoundariesButNoBoundaryValuePlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
            super(sc, lr, cp);
        }

        @Override
        public List<String> getSchemaTypesThePlaybookWillApplyTo() {
            return List.of("string");
        }

        @Override
        public String getBoundaryValue(Schema schema) {
            return null;
        }

        @Override
        public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
            return true;
        }

        @Override
        public String description() {
            return "simple description";
        }
    }

    static class MyBaseBoundaryWithoutBoundariesPlaybook extends BaseBoundaryFieldPlaybook {

        public MyBaseBoundaryWithoutBoundariesPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
            super(sc, lr, cp);
        }

        @Override
        public List<String> getSchemaTypesThePlaybookWillApplyTo() {
            return List.of("string");
        }

        @Override
        public String getBoundaryValue(Schema schema) {
            return "test";
        }

        @Override
        public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
            return false;
        }

        @Override
        public String description() {
            return "simple description";
        }
    }

    static class MyBaseBoundaryWithBoundariesAndIntegerSchemaPlaybook extends BaseBoundaryFieldPlaybook {

        public MyBaseBoundaryWithBoundariesAndIntegerSchemaPlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
            super(sc, lr, cp);
        }

        @Override
        public List<String> getSchemaTypesThePlaybookWillApplyTo() {
            return List.of("integer");
        }

        @Override
        public String getBoundaryValue(Schema schema) {
            return "test";
        }

        @Override
        public boolean hasBoundaryDefined(String fuzzedField, PlaybookData data) {
            return true;
        }

        @Override
        public String description() {
            return "simple description";
        }
    }
}
