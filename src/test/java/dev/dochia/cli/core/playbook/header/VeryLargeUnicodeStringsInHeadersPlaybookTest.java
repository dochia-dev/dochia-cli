package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.args.MatchArguments;
import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class VeryLargeUnicodeStringsInHeadersPlaybookTest {
    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private ProcessingArguments processingArguments;

    private VeryLargeUnicodeStringsInHeadersPlaybook veryLargeUnicodeStringsInHeadersPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        processingArguments = Mockito.mock(ProcessingArguments.class);
        HeadersIteratorExecutor headersIteratorExecutor = new HeadersIteratorExecutor(serviceCaller, testCaseListener, Mockito.mock(MatchArguments.class), Mockito.mock(FilterArguments.class));
        veryLargeUnicodeStringsInHeadersPlaybook = new VeryLargeUnicodeStringsInHeadersPlaybook(headersIteratorExecutor, processingArguments);
    }

    @Test
    void givenANewLargeValuesInHeadersPlaybook_whenCreatingANewInstance_thenTheMethodsBeingOverriddenAreMatchingTheLargeValuesInHeadersPlaybook() {
        Assertions.assertThat(veryLargeUnicodeStringsInHeadersPlaybook.description()).isNotNull();
        Assertions.assertThat(veryLargeUnicodeStringsInHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isNotNull();
        Assertions.assertThat(veryLargeUnicodeStringsInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo(FuzzingStrategy.replace().name());
    }

    @Test
    void shouldNotMatchResponse() {
        Assertions.assertThat(veryLargeUnicodeStringsInHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isFalse();
    }

    @Test
    void shouldGetPayloadSize() {
        Mockito.when(processingArguments.getLargeStringsSize()).thenReturn(20);
        Assertions.assertThat(veryLargeUnicodeStringsInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().getData().toString()).hasSize(20 + "dochia".length());
    }

    @Test
    void shouldNotMatchContentType() {
        Assertions.assertThat(veryLargeUnicodeStringsInHeadersPlaybook.getPlaybookContext().isMatchResponseContentType()).isFalse();
    }
}