package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.MatchArguments;
import dev.dochia.cli.core.context.GlobalContext;
import dev.dochia.cli.core.playbook.executor.FieldsIteratorExecutor;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

@QuarkusTest
class SwapDiscriminatorValuesFieldsPlaybookTest
 {
    @InjectSpy
    TestCaseListener testCaseListener;
    FieldsIteratorExecutor executor;
    private SwapDiscriminatorValuesFieldsPlaybook swapDiscriminatorValuesFieldsPlaybook;

    @Inject
    GlobalContext globalContext;

    @BeforeEach
    void setup() {
        executor = new FieldsIteratorExecutor(Mockito.mock(ServiceCaller.class), testCaseListener, Mockito.mock(MatchArguments.class), Mockito.mock(FilesArguments.class));
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        swapDiscriminatorValuesFieldsPlaybook = new SwapDiscriminatorValuesFieldsPlaybook(executor, globalContext);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(swapDiscriminatorValuesFieldsPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(swapDiscriminatorValuesFieldsPlaybook).hasToString("SwapDiscriminatorValuesFieldsPlaybook");
    }

    @Test
    void shouldSkipForNonBodyMethods() {
        Assertions.assertThat(swapDiscriminatorValuesFieldsPlaybook.skipForHttpMethods())
                .contains(HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD);
    }

    @Test
    void shouldSkipIfFieldNotDiscriminator() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("nonDiscriminatorField"));
        Mockito.when(data.getPayload()).thenReturn("""
                    {"nonDiscriminatorField": "value"}
                """);
        swapDiscriminatorValuesFieldsPlaybook.run(data);
        Mockito.verifyNoInteractions(testCaseListener);
    }

    @Test
    void shouldRunIfFieldDiscriminator() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("discriminatorField"));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("discriminatorField", new Schema()));

        Mockito.when(data.getPayload()).thenReturn("""
                   {"discriminatorField": "oldValue"}
                """);
        globalContext.recordDiscriminator("", new Discriminator().propertyName("discriminatorField"), List.of("oldValue", "newValue"));

        swapDiscriminatorValuesFieldsPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(1)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any());
    }
}
