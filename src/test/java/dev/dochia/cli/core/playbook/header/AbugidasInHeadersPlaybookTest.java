package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.args.MatchArguments;
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
class AbugidasInHeadersPlaybookTest {
    private AbugidasInHeadersPlaybook abugidasInHeadersPlaybook;

    @BeforeEach
    void setup() {
        ServiceCaller serviceCaller = Mockito.mock(ServiceCaller.class);
        TestCaseListener testCaseListener = Mockito.mock(TestCaseListener.class);
        HeadersIteratorExecutor headersIteratorExecutor = new HeadersIteratorExecutor(serviceCaller, testCaseListener, Mockito.mock(MatchArguments.class), Mockito.mock(FilterArguments.class));
        abugidasInHeadersPlaybook = new AbugidasInHeadersPlaybook(headersIteratorExecutor);
    }

    @Test
    void shouldHaveAllMethodsOverridden() {
        Assertions.assertThat(abugidasInHeadersPlaybook.description()).isNotNull();
        Assertions.assertThat(abugidasInHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isNotNull();
        Assertions.assertThat(abugidasInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo(FuzzingStrategy.replace().name());
        Assertions.assertThat(abugidasInHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isFalse();
    }

    @Test
    void shouldGetZalgoTextAsPayload() {
        Assertions.assertThat(abugidasInHeadersPlaybook.getPlaybookContext().getFuzzStrategy()).hasSize(2);
        Assertions.assertThat(abugidasInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().getData()).isEqualTo("జ్ఞ\u200Cా");
    }
}