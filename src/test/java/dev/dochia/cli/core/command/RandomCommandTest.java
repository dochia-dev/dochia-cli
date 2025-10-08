package dev.dochia.cli.core.command;

import dev.dochia.cli.core.args.*;
import dev.dochia.cli.core.http.HttpMethod;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import picocli.CommandLine;

@QuarkusTest
class RandomCommandTest {

    @Inject
    RandomCommand randomCommand;

    @BeforeEach
    void setup() {
        CommandLine.Model.CommandSpec spec = Mockito.mock(CommandLine.Model.CommandSpec.class);
        Mockito.when(spec.commandLine()).thenReturn(Mockito.mock(CommandLine.class));
        ReflectionTestUtils.setField(randomCommand, "spec", spec);
    }

    @Test
    void shouldRunWithAllArguments() {
        ApiArguments apiArguments = new ApiArguments();
        apiArguments.setContract("contract");
        apiArguments.setServer("http://server");
        randomCommand.apiArguments = apiArguments;

        MatchArguments matchArguments = Mockito.mock(MatchArguments.class);
        Mockito.when(matchArguments.isAnyMatchArgumentSupplied()).thenReturn(true);

        StopArguments stopArguments = Mockito.mock(StopArguments.class);
        Mockito.when(stopArguments.isAnyStopConditionProvided()).thenReturn(true);

        randomCommand.stopArguments = stopArguments;
        randomCommand.matchArguments = matchArguments;
        randomCommand.path = "/path";
        randomCommand.httpMethod = HttpMethod.POST;

        TestCommand testCommand = Mockito.mock(TestCommand.class);
        randomCommand.testCommand = testCommand;
        testCommand.filterArguments = Mockito.mock(FilterArguments.class);
        testCommand.processingArguments = Mockito.mock(ProcessingArguments.class);
        randomCommand.run();
        Mockito.verify(testCommand, Mockito.times(1)).run();
    }

    @Test
    void shouldReturnNonZeroExitCode() {
        TestCommand testCommand = Mockito.mock(TestCommand.class);
        Mockito.when(testCommand.getExitCode()).thenReturn(10);
        randomCommand.testCommand = testCommand;
        int exitCode = randomCommand.getExitCode();
        Assertions.assertThat(exitCode).isEqualTo(10);
    }

    @Test
    void shouldThrowExceptionWhenServerNotValid() {
        ApiArguments apiArguments = new ApiArguments();
        apiArguments.setContract("contract");
        apiArguments.setServer("server");
        randomCommand.apiArguments = apiArguments;
        Assertions.assertThatThrownBy(() -> randomCommand.apiArguments.validateValidServer(randomCommand.spec, null))
                .isInstanceOf(CommandLine.ParameterException.class);
    }
}
