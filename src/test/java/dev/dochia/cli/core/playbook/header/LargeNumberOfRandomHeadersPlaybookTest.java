package dev.dochia.cli.core.playbook.header;


import dev.dochia.cli.core.args.ProcessingArguments;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

@QuarkusTest
class LargeNumberOfRandomHeadersPlaybookTest {

    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private LargeNumberOfRandomHeadersPlaybook largeNumberOfRandomHeadersPlaybook;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        SimpleExecutor simpleExecutor = new SimpleExecutor(testCaseListener, serviceCaller);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        ProcessingArguments processingArguments = Mockito.mock(ProcessingArguments.class);
        Mockito.when(processingArguments.getRandomHeadersNumber()).thenReturn(10000);
        largeNumberOfRandomHeadersPlaybook = new LargeNumberOfRandomHeadersPlaybook(simpleExecutor, testCaseListener, processingArguments);
    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(largeNumberOfRandomHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldReturnRandomHeaderValues() {
        Assertions.assertThat(largeNumberOfRandomHeadersPlaybook.randomHeadersValueFunction().apply(10)).isNotBlank();
    }
}
