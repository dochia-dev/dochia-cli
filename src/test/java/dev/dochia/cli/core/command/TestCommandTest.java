package dev.dochia.cli.core.command;

import dev.dochia.cli.core.args.*;
import dev.dochia.cli.core.command.model.ConfigOptions;
import dev.dochia.cli.core.context.GlobalContext;
import dev.dochia.cli.core.factory.PlaybookDataFactory;
import dev.dochia.cli.core.playbook.body.HappyPathPlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.stateful.DeletedResourcesNotAvailablePlaybook;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.report.ExecutionStatisticsListener;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import dev.dochia.cli.core.util.VersionChecker;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Future;

@QuarkusTest
class TestCommandTest {

    @Inject
    TestCommand testCommand;
    @InjectSpy
    ExecutionStatisticsListener executionStatisticsListener;
    @Inject
    CheckArguments checkArguments;
    @Inject
    ReportingArguments reportingArguments;
    @Inject
    ApiArguments apiArguments;
    @InjectSpy
    PlaybookDataFactory playbookDataFactory;
    @Inject
    ProcessingArguments processingArguments;
    @Inject
    ConfigOptions configOptions;
    @InjectSpy
    private TestCaseListener testCaseListener;
    private FilterArguments filterArguments;
    SimpleExecutor simpleExecutor;

    @BeforeEach
    void setup() {
        filterArguments = Mockito.mock(FilterArguments.class);
        ReflectionTestUtils.setField(testCommand, "filterArguments", filterArguments);
        Mockito.when(filterArguments.getHttpMethods()).thenReturn(HttpMethod.restMethods());
        ReflectionTestUtils.setField(reportingArguments, "verbosity", ReportingArguments.Verbosity.DETAILED);
        ReflectionTestUtils.setField(testCommand, "spec", Mockito.mock(CommandLine.Model.CommandSpec.class));
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        simpleExecutor = new SimpleExecutor(testCaseListener, Mockito.mock(ServiceCaller.class));
    }

    @Test
    void shouldCheckForNewVersion() throws Exception {
        ReportingArguments repArgs = Mockito.mock(ReportingArguments.class);
        Mockito.when(repArgs.isCheckUpdate()).thenReturn(true);
        ReflectionTestUtils.setField(testCommand, "reportingArguments", repArgs);
        VersionChecker checker = Mockito.mock(VersionChecker.class);
        Mockito.when(checker.checkForNewVersion(Mockito.anyString())).thenReturn(VersionChecker.CheckResult.builder().newVersion(true).version("1.0.0").build());
        ReflectionTestUtils.setField(testCommand, "versionChecker", checker);

        Future<VersionChecker.CheckResult> resultFuture = testCommand.checkForNewVersion();
        testCommand.printVersion(resultFuture);

        VersionChecker.CheckResult result = resultFuture.get();
        Assertions.assertThat(result.isNewVersion()).isTrue();
        Assertions.assertThat(result.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    void shouldNotCheckForNewVersion() throws Exception {
        ReportingArguments repArgs = Mockito.mock(ReportingArguments.class);
        Mockito.when(repArgs.isCheckUpdate()).thenReturn(false);
        ReflectionTestUtils.setField(testCommand, "reportingArguments", repArgs);
        VersionChecker checker = Mockito.mock(VersionChecker.class);
        Mockito.when(checker.checkForNewVersion(Mockito.anyString())).thenReturn(VersionChecker.CheckResult.builder().newVersion(true).version("1.0.0").build());
        ReflectionTestUtils.setField(testCommand, "versionChecker", checker);

        Future<VersionChecker.CheckResult> resultFuture = testCommand.checkForNewVersion();
        testCommand.printVersion(resultFuture);

        VersionChecker.CheckResult result = resultFuture.get();
        Assertions.assertThat(result.isNewVersion()).isFalse();
        Assertions.assertThat(result.getVersion()).isNotEqualTo("1.0.0");
    }

    @Test
    void shouldNotRunWhenNotRecognizedContentType() throws Exception {
        ReflectionTestUtils.setField(apiArguments, "contract", "src/test/resources/petstore-nonjson.yml");
        ReflectionTestUtils.setField(apiArguments, "server", "http://localhost:8080");


        TestCommand spyMain = Mockito.spy(testCommand);
        spyMain.run();
        Mockito.verify(spyMain).createOpenAPI();
        Mockito.verify(spyMain).startFuzzing(Mockito.any());
        Mockito.verify(testCaseListener, Mockito.times(1)).startSession();
        Mockito.verify(testCaseListener, Mockito.times(1)).endSession();
        Mockito.verify(testCaseListener, Mockito.times(0)).afterFuzz(Mockito.any());
        ReflectionTestUtils.setField(apiArguments, "contract", "empty");
        ReflectionTestUtils.setField(apiArguments, "server", "empty");
    }

    @Test
    void shouldNotCallEndSessionWhenIOException() {
        ReflectionTestUtils.setField(apiArguments, "contract", "src/test/resources/not_existent.yml");
        ReflectionTestUtils.setField(apiArguments, "server", "http://localhost:8080");

        testCommand.run();
        Mockito.verify(testCaseListener, Mockito.times(1)).startSession();
        Mockito.verify(testCaseListener, Mockito.times(0)).endSession();
        Mockito.verify(testCaseListener, Mockito.times(0)).afterFuzz(Mockito.any());
    }

    @Test
    void givenContractAndServerParameter_whenStartingDochia_thenParametersAreProcessedSuccessfully() throws Exception {
        ReflectionTestUtils.setField(apiArguments, "contract", "src/test/resources/petstore.yml");
        ReflectionTestUtils.setField(apiArguments, "server", "http://localhost:8080");
        ReflectionTestUtils.setField(reportingArguments, "logData", List.of("org.apache.wire:debug", "dev.dochia.cli:warn", "error"));
        ReflectionTestUtils.setField(reportingArguments, "skipLogs", List.of("complete", "notSkip"));
        ReflectionTestUtils.setField(processingArguments, "useExamples", false);
        ReflectionTestUtils.setField(reportingArguments, "debug", true);

        Mockito.when(filterArguments.getFirstPhasePlaybooksForPath()).thenReturn(List.of("HappyPathPlaybook"));
        Mockito.when(filterArguments.getSuppliedPlaybooks()).thenReturn(List.of("HappyPathPlaybook"));
        Mockito.when(filterArguments.isHttpMethodSupplied(Mockito.any())).thenReturn(true);
        Mockito.when(filterArguments.filterOutPlaybooksNotMatchingHttpMethodsAndPath(Mockito.any(), Mockito.anyString())).thenReturn(List.of(new HappyPathPlaybook(simpleExecutor)));
        Mockito.when(filterArguments.getSecondPhasePlaybooks()).thenReturn(List.of(Mockito.mock(DeletedResourcesNotAvailablePlaybook.class)));
        Mockito.when(executionStatisticsListener.areManyIoErrors()).thenReturn(true);
        Mockito.when(executionStatisticsListener.getIoErrors()).thenReturn(10);

        Mockito.when(filterArguments.getPathsToRun(Mockito.any())).thenReturn(List.of("/pet-types", "/pet-types-rec", "/pets", "/pets-batch", "/pets/{id}"));
        TestCommand spyMain = Mockito.spy(testCommand);
        spyMain.run();
        Mockito.verify(spyMain).createOpenAPI();
        Mockito.verify(spyMain).startFuzzing(Mockito.any());
        Mockito.verify(testCaseListener, Mockito.times(1)).startSession();
        Mockito.verify(testCaseListener, Mockito.times(1)).endSession();
        Mockito.verify(testCaseListener, Mockito.times(20)).afterFuzz(Mockito.any());
        Mockito.verify(testCaseListener, Mockito.times(10)).beforeFuzz(Mockito.eq(HappyPathPlaybook.class), Mockito.any(), Mockito.any());

        ReflectionTestUtils.setField(apiArguments, "contract", "empty");
        ReflectionTestUtils.setField(apiArguments, "server", "empty");
    }

    @Test
    void givenAnOpenApiContract_whenStartingDochia_thenTheContractIsCorrectlyParsed() throws Exception {
        ReflectionTestUtils.setField(apiArguments, "contract", "src/test/resources/openapi.yml");
        ReflectionTestUtils.setField(apiArguments, "server", "http://localhost:8080");
        ReflectionTestUtils.setField(checkArguments, "includeEmojis", true);
        ReflectionTestUtils.setField(reportingArguments, "verbosity", ReportingArguments.Verbosity.SUMMARY);

        TestCommand spyMain = Mockito.spy(testCommand);
        Mockito.when(filterArguments.getFirstPhasePlaybooksForPath()).thenReturn(List.of("HappyPathPlaybook"));
        Mockito.when(filterArguments.isHttpMethodSupplied(Mockito.any())).thenReturn(true);
        Mockito.when(filterArguments.filterOutPlaybooksNotMatchingHttpMethodsAndPath(Mockito.any(), Mockito.anyString())).thenReturn(List.of(new HappyPathPlaybook(simpleExecutor)));
        Mockito.when(filterArguments.getSecondPhasePlaybooks()).thenReturn(List.of(new DeletedResourcesNotAvailablePlaybook(null, Mockito.mock(GlobalContext.class), null)));
        Mockito.when(filterArguments.getPathsToRun(Mockito.any())).thenReturn(
                List.of("/pet", "/pets", "/pet/findByStatus", "/pet/findByTags", "/pet/{petId}", "/pet/{petId}/uploadImage", "/store/inventory"));
        Mockito.when(executionStatisticsListener.areManyAuthErrors()).thenReturn(true);
        Mockito.when(executionStatisticsListener.getAuthErrors()).thenReturn(9);

        spyMain.run();
        Mockito.verify(spyMain).createOpenAPI();
        Mockito.verify(spyMain).startFuzzing(Mockito.any());
        Mockito.verify(playbookDataFactory).fromPathItem(Mockito.eq("/pet"), Mockito.any(), Mockito.any());
        Mockito.verify(playbookDataFactory, Mockito.times(0)).fromPathItem(Mockito.eq("/petss"), Mockito.any(), Mockito.any());
        Mockito.verify(testCaseListener, Mockito.times(13)).afterFuzz(Mockito.any());
        Mockito.verify(testCaseListener, Mockito.times(9)).beforeFuzz(Mockito.eq(HappyPathPlaybook.class), Mockito.anyString(), Mockito.any());
        Mockito.verify(testCaseListener, Mockito.times(4)).beforeFuzz(Mockito.eq(DeletedResourcesNotAvailablePlaybook.class), Mockito.any(), Mockito.any());

        ReflectionTestUtils.setField(apiArguments, "contract", "empty");
        ReflectionTestUtils.setField(apiArguments, "server", "empty");
    }

    @Test
    void shouldReturnErrorsExitCode() {
        Mockito.when(executionStatisticsListener.getErrors()).thenReturn(190);

        Assertions.assertThat(testCommand.getExitCode()).isEqualTo(190);
    }

    @Test
    void shouldThrowExceptionWhenNoContract() {
        CommandLine.Model.CommandSpec spec = Mockito.mock(CommandLine.Model.CommandSpec.class);
        Mockito.when(spec.commandLine()).thenReturn(Mockito.mock(CommandLine.class));
        ReflectionTestUtils.setField(apiArguments, "contract", null);
        ReflectionTestUtils.setField(testCommand, "spec", spec);

        ReflectionTestUtils.setField(apiArguments, "server", "http://localhost:8080");
        Assertions.assertThatThrownBy(() -> testCommand.run()).isInstanceOf(CommandLine.ParameterException.class).hasMessage("Missing required option --contract=<contract>");
    }

    @Test
    void shouldThrowExceptionWhenServerNotValid() {
        CommandLine.Model.CommandSpec spec = Mockito.mock(CommandLine.Model.CommandSpec.class);
        Mockito.when(spec.commandLine()).thenReturn(Mockito.mock(CommandLine.class));
        ReflectionTestUtils.setField(testCommand, "spec", spec);
        ReflectionTestUtils.setField(apiArguments, "server", "server");
        Assertions.assertThatThrownBy(() -> testCommand.run()).isInstanceOf(CommandLine.ParameterException.class);
    }
}
