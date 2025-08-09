package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkusTest
class FullwidthBracketsFieldsPlaybookTest
 {
    private FullwidthBracketsFieldsPlaybook fullwidthBracketsFieldsPlaybook;

    @BeforeEach
    void setup() {
        ServiceCaller serviceCaller = Mockito.mock(ServiceCaller.class);
        TestCaseListener testCaseListener = Mockito.mock(TestCaseListener.class);
        FilesArguments filesArguments = Mockito.mock(FilesArguments.class);
        fullwidthBracketsFieldsPlaybook = new FullwidthBracketsFieldsPlaybook(serviceCaller, testCaseListener, filesArguments);
    }

    private PlaybookData mockFuzzingData() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Map<String, Schema> reqTypes = new HashMap<>();
        reqTypes.put("testField", new StringSchema());
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(reqTypes);
        return data;
    }

    @Test
    void shouldHaveAllMethodsOverridden() {
        Assertions.assertThat(fullwidthBracketsFieldsPlaybook.description()).isNotNull();
        Assertions.assertThat(fullwidthBracketsFieldsPlaybook.typeOfDataSentToTheService()).isEqualTo("fullwidth angle bracket characters");
    }

    @Test
    void shouldGetFullwidthBracketsAsPayload() {
        PlaybookData data = mockFuzzingData();
        List<FuzzingStrategy> strategies = fullwidthBracketsFieldsPlaybook.getFieldFuzzingStrategy(data, "testField");
        Assertions.assertThat(strategies).hasSize(2);
    }

    @Test
    void shouldSkipFuzzingForSpecialChars() {
        PlaybookData data = mockFuzzingData();
        Assertions.assertThat(fullwidthBracketsFieldsPlaybook.isPlaybookApplicable(data, "testField")).isFalse();
    }
}
