package dev.dochia.cli.core.playbook.special;

import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.args.MatchArguments;
import dev.dochia.cli.core.args.StopArguments;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.special.mutators.api.Mutator;
import dev.dochia.cli.core.report.ExecutionStatisticsListener;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.Set;

@QuarkusTest
class RandomPlaybookTest {
    private SimpleExecutor simpleExecutor;
    private TestCaseListener testCaseListener;
    private ExecutionStatisticsListener executionStatisticsListener;
    private MatchArguments matchArguments;
    private StopArguments stopArguments;
    private RandomPlaybook randomPlaybook;
    private FilesArguments filesArguments;
    @Inject
    Instance<Mutator> mutators;

    @BeforeEach
    void setup() {
        simpleExecutor = Mockito.mock(SimpleExecutor.class);
        stopArguments = Mockito.mock(StopArguments.class);
        executionStatisticsListener = Mockito.mock(ExecutionStatisticsListener.class);
        matchArguments = Mockito.mock(MatchArguments.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);

        randomPlaybook = new RandomPlaybook(simpleExecutor, testCaseListener,
                executionStatisticsListener,
                matchArguments, mutators,
                stopArguments, filesArguments);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
    }

    @Test
    void shouldNotRunWhenEmptyPayload() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        randomPlaybook.run(data);
        Mockito.verifyNoInteractions(simpleExecutor);
    }

    @Test
    void shouldRunWhenPayloadNotEmpty() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getPayload()).thenReturn("{\"id\":\"value\"}");
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("id"));
        Mockito.when(stopArguments.shouldStop(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
        randomPlaybook.run(data);
        Mockito.verify(simpleExecutor, Mockito.times(1)).execute(Mockito.any());
    }

    @Test
    void shouldRunForMultipleTimes() {
        PlaybookData data = mockData();
        randomPlaybook.run(data);
        Mockito.verify(simpleExecutor, Mockito.times(3)).execute(Mockito.any());
    }

    @Test
    void shouldReportError() {
        HttpResponse response = HttpResponse.empty();
        PlaybookData data = PlaybookData.builder().build();
        Mockito.when(matchArguments.isMatchResponse(response)).thenReturn(true);
        Mockito.when(matchArguments.getMatchString()).thenReturn(" test");

        randomPlaybook.processResponse(response, data);

        Mockito.verify(testCaseListener, Mockito.times(1))
                .reportResultError(Mockito.any(), Mockito.eq(data),
                        Mockito.eq("Response matches arguments"), Mockito.eq("Response matches test"));
    }

    @Test
    void shouldReportSkip() {
        HttpResponse response = HttpResponse.empty();
        PlaybookData data = PlaybookData.builder().build();

        randomPlaybook.processResponse(response, data);

        Mockito.verify(testCaseListener, Mockito.times(1))
                .skipTest(Mockito.any(), Mockito.eq("Skipping test as response does not match given matchers!"));
    }

    @Test
    void shouldOverrideMethods() {
        Assertions.assertThat(randomPlaybook.description()).isNotBlank();
        Assertions.assertThat(randomPlaybook).hasToString(RandomPlaybook.class.getSimpleName());
    }

    @Test
    void shouldNotRunAnyTestWhenEmptyMutators() {
        Mockito.when(filesArguments.getMutatorsFolder()).thenReturn(new File("src/tests"));
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getPayload()).thenReturn("{\"id\":\"value\"}");
        randomPlaybook.run(data);
        Mockito.verifyNoInteractions(simpleExecutor);
    }

    @Test
    void shouldRunCustomMutatorsFromValidFolder() throws Exception {
        Mockito.when(filesArguments.getMutatorsFolder()).thenReturn(new File("src/test/resources/mutators"));
        PlaybookData data = mockData();
        RandomPlaybook randomPlaybookSpy = Mockito.spy(randomPlaybook);
        randomPlaybookSpy.run(data);
        Mockito.verify(simpleExecutor, Mockito.times(3)).execute(Mockito.any());
        Mockito.verify(randomPlaybookSpy, Mockito.times(3)).createConfig(Mockito.any());
        Mockito.verify(randomPlaybookSpy, Mockito.times(1)).readValueFromFile(Mockito.endsWith("dict.txt"));
    }

    @NotNull
    private PlaybookData mockData() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getPayload()).thenReturn("{\"id\":\"value\"}");
        Mockito.when(data.getMethod()).thenReturn(HttpMethod.POST);
        Mockito.when(data.getPath()).thenReturn("/path");
        Mockito.when(data.getAllFieldsByHttpMethod()).thenReturn(Set.of("id"));
        Mockito.when(stopArguments.shouldStop(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(false).thenReturn(false).thenReturn(true);
        return data;
    }
}
