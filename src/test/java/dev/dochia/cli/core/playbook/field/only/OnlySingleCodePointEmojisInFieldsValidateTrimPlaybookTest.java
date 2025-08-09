package dev.dochia.cli.core.playbook.field.only;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

@QuarkusTest
class OnlySingleCodePointEmojisInFieldsValidateTrimPlaybookTest
 {
    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;
    private FilterArguments filterArguments;

    private OnlySingleCodePointEmojisInFieldsValidateTrimPlaybook onlySingleCodePointEmojisInFieldsValidateTrimPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        filterArguments = Mockito.mock(FilterArguments.class);
        onlySingleCodePointEmojisInFieldsValidateTrimPlaybook = new OnlySingleCodePointEmojisInFieldsValidateTrimPlaybook(serviceCaller, testCaseListener, filesArguments, filterArguments);
    }

    @Test
    void shouldOverrideDefaultMethods() {
        Assertions.assertThat(onlySingleCodePointEmojisInFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(onlySingleCodePointEmojisInFieldsValidateTrimPlaybook.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(onlySingleCodePointEmojisInFieldsValidateTrimPlaybook.skipForHttpMethods()).isEmpty();
        Assertions.assertThat(onlySingleCodePointEmojisInFieldsValidateTrimPlaybook.description()).isNotNull();
        Assertions.assertThat(onlySingleCodePointEmojisInFieldsValidateTrimPlaybook.typeOfDataSentToTheService()).isNotNull();
        Assertions.assertThat(onlySingleCodePointEmojisInFieldsValidateTrimPlaybook.getInvisibleChars()).contains("\uD83D\uDC7E");
    }

    @Test
    void shouldReturnProperLengthWhenNoMinLLength() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Map<String, Schema> schemaMap = new HashMap<>();
        StringSchema stringSchema = new StringSchema();
        schemaMap.put("schema", stringSchema);
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(schemaMap);

        FuzzingStrategy fuzzingStrategy = onlySingleCodePointEmojisInFieldsValidateTrimPlaybook.getFieldFuzzingStrategy(data, "schema").get(1);
        Assertions.assertThat(fuzzingStrategy.name()).isEqualTo(FuzzingStrategy.replace().name());
        Assertions.assertThat(fuzzingStrategy.getData()).isEqualTo("\uD83D\uDC80");
    }

    @Test
    void shouldReturnProperLengthWhenMinValue() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Map<String, Schema> schemaMap = new HashMap<>();
        StringSchema stringSchema = new StringSchema();
        stringSchema.setMinLength(5);

        schemaMap.put("schema", stringSchema);
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(schemaMap);

        FuzzingStrategy fuzzingStrategy = onlySingleCodePointEmojisInFieldsValidateTrimPlaybook.getFieldFuzzingStrategy(data, "schema").get(1);
        Assertions.assertThat(fuzzingStrategy.name()).isEqualTo(FuzzingStrategy.replace().name());
        Assertions.assertThat(fuzzingStrategy.getData()).isEqualTo(StringUtils.repeat("\uD83D\uDC80", (stringSchema.getMinLength() / 2) + 1));
    }
}
