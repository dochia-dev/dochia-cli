package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.Set;

@QuarkusTest
class ZeroWidthCharsInNamesFieldsPlaybookTest {
    private ZeroWidthCharsInNamesFieldsPlaybook zeroWidthCharsInNamesFieldsPlaybook;
    private SimpleExecutor simpleExecutor;

    @BeforeEach
    void setup() {
        simpleExecutor = Mockito.mock(SimpleExecutor.class);
        zeroWidthCharsInNamesFieldsPlaybook = new ZeroWidthCharsInNamesFieldsPlaybook(simpleExecutor);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(zeroWidthCharsInNamesFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(zeroWidthCharsInNamesFieldsPlaybook).hasToString(zeroWidthCharsInNamesFieldsPlaybook.getClass().getSimpleName());
    }

    @ParameterizedTest
    @CsvSource(value = {"''", "null"}, nullValues = "null")
    void shouldNotRunWithEmptyPayload(String payload) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getPayload()).thenReturn(payload);
        zeroWidthCharsInNamesFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(simpleExecutor);
    }

    @Test
    void shouldNotRunWhenNoFields() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of());
        zeroWidthCharsInNamesFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(simpleExecutor);
    }

    @Test
    void shouldRunWhenFieldsPresent() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("name", "lastName", "address#zip"));
        Mockito.when(data.getPayload()).thenReturn("""
                {
                    "name": "John",
                    "lastName": "Doe",
                    "address": {
                        "zip": "12345"
                    }
                }
                """);
        zeroWidthCharsInNamesFieldsPlaybook.run(data);
        Mockito.verify(simpleExecutor, Mockito.times(18)).execute(Mockito.any());
    }

    @Test
    void shouldSkipForHttpMethods() {
        Assertions.assertThat(zeroWidthCharsInNamesFieldsPlaybook.skipForHttpMethods()).containsOnly(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.TRACE);
    }
}
