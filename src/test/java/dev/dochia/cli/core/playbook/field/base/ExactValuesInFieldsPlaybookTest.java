package dev.dochia.cli.core.playbook.field.base;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.generator.simple.StringGenerator;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@QuarkusTest
class ExactValuesInFieldsPlaybookTest
 {

    private ExactValuesInFieldsPlaybook myBaseBoundaryPlaybook;
    private FilesArguments filesArguments;

    @BeforeEach
    void setup() {
        filesArguments = Mockito.mock(FilesArguments.class);
        myBaseBoundaryPlaybook = new MyExactValuePlaybook(null, null, filesArguments);
    }

    @Test
    void shouldNotRunWhenRefData() {
        Mockito.when(filesArguments.getRefData("/test")).thenReturn(Map.of("test", "value"));
        StringSchema stringSchema = new StringSchema();
        PlaybookData data = PlaybookData.builder().path("/test").requestPropertyTypes(Collections.singletonMap("test", stringSchema)).build();
        stringSchema.setMaximum(new BigDecimal(100));
        Assertions.assertThat(myBaseBoundaryPlaybook.hasBoundaryDefined("test", data)).isFalse();
    }

    @Test
    void shouldGetBoundaryValueForSchemaWithPattern() {
        Schema<String> schema = new StringSchema();
        schema.setPattern("[0-9]+");
        schema.setMaxLength(10);
        Object generated = myBaseBoundaryPlaybook.getBoundaryValue(schema);

        Assertions.assertThat(generated).asString().matches("[0-9]+");
    }

    @Test
    void shouldGetBoundaryValueForSchemaWithNoPattern() {
        Schema<String> schema = new StringSchema();
        schema.setMaxLength(10);
        Object generated = myBaseBoundaryPlaybook.getBoundaryValue(schema);

        Assertions.assertThat(generated).asString().matches(StringGenerator.ALPHANUMERIC_PLUS);
    }

    @Test
    void shouldGetBoundaryValueForSchemaWithPatternUsingRegexGen() {
        Schema<String> schema = new StringSchema();
        schema.setPattern("^(A-\\d{1,12})$");
        schema.setMaxLength(14);
        Object generated = myBaseBoundaryPlaybook.getBoundaryValue(schema);

        Assertions.assertThat(generated).asString().matches("^(A-\\d{1,12})$");
    }

    @Test
    void shouldGetNullBoundaryValueWhenNoBoundaries() {
        Schema<String> schema = new StringSchema();
        Object generated = myBaseBoundaryPlaybook.getBoundaryValue(schema);

        Assertions.assertThat(generated).isNull();
    }

    @Test
    void shouldGetBase64EncodeWhenByteArray() {
        Schema<byte[]> schema = new ByteArraySchema();
        schema.setMaxLength(10);
        Object generated = myBaseBoundaryPlaybook.getBoundaryValue(schema);

        Assertions.assertThat(generated).asString().matches("^[A-Za-z0-9+/=]+\\Z");
    }

    @Test
    void shouldGenerateBoundaryValueWhenIllegalArgumentExceptionIsThrown() {
        Schema<String> schema = new StringSchema();
        String pattern = "^[A-Z-a-z0-9]{4}[A-Z-a-z]{2}[A-Z-a-z0-9]{2}([A-Z-a-z0-9]{3})?$";
        schema.setMaxLength(11);
        schema.setPattern(pattern);
        Object generated = myBaseBoundaryPlaybook.getBoundaryValue(schema);

        Assertions.assertThat(generated).asString().matches(pattern);
    }

    @Test
    void shouldGenerateWithIntegerMaxInt() {
        Schema<String> schema = new StringSchema();
        schema.setPattern("[0-9]+");
        schema.setMaxLength(Integer.MAX_VALUE);
        Object generated = myBaseBoundaryPlaybook.getBoundaryValue(schema);

        Assertions.assertThat(generated).asString().matches("[0-9]+");
    }

    @Test
    void shouldGenerateWithIntegerMinInt() {
        Schema<String> schema = new StringSchema();
        myBaseBoundaryPlaybook = new MyExactMinValuePlaybook(null, null, filesArguments);
        schema.setPattern("[0-9]+");
        schema.setMaxLength(Integer.MAX_VALUE);
        schema.setMinLength(Integer.MIN_VALUE);
        Object generated = myBaseBoundaryPlaybook.getBoundaryValue(schema);

        Assertions.assertThat(generated).asString().isEmpty();
    }

    @Test
    void shouldNotGenerateWhenInvalidRegex() {
        Schema<String> schema = new StringSchema();
        TestCaseListener testCaseListener = Mockito.mock(TestCaseListener.class);
        myBaseBoundaryPlaybook = new MyExactMinValuePlaybook(null, testCaseListener, filesArguments);
        schema.setPattern("[0-9][sss-000]+");
        schema.setMaxLength(4);
        schema.setMinLength(2);
        Object generated = myBaseBoundaryPlaybook.getBoundaryValue(schema);
        Mockito.verify(testCaseListener).recordError("Playbook %s could not generate a value for patten %s, min %s, max %s"
                .formatted(myBaseBoundaryPlaybook.getClass().getSimpleName(), schema.getPattern(), schema.getMinLength(), schema.getMaxLength()));
        Assertions.assertThat(generated).isNull();
    }

    static class MyExactValuePlaybook extends ExactValuesInFieldsPlaybook {

        public MyExactValuePlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
            super(sc, lr, cp);
        }

        @Override
        protected String exactValueTypeString() {
            return null;
        }

        @Override
        protected Function<Schema, Number> getExactMethod() {
            return Schema::getMaxLength;
        }
    }

    static class MyExactMinValuePlaybook extends ExactValuesInFieldsPlaybook {

        public MyExactMinValuePlaybook(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
            super(sc, lr, cp);
        }

        @Override
        protected String exactValueTypeString() {
            return null;
        }

        @Override
        protected Function<Schema, Number> getExactMethod() {
            return Schema::getMinLength;
        }
    }
}
