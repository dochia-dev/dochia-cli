package dev.dochia.cli.core.command;

import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.special.mutators.api.Mutator;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import picocli.CommandLine;

@QuarkusTest
class ListCommandTest {

    @Inject
    Instance<TestCasePlaybook> playbooks;

    @Inject
    Instance<OpenAPIFormat> formats;

    @Inject
    Instance<Mutator> mutators;

    private ListCommand listCommand;

    @BeforeEach
    void setup() {
        listCommand = new ListCommand(playbooks, formats, mutators);
    }

    @Test
    void shouldListFormats() {
        ListCommand spyListCommand = Mockito.spy(listCommand);
        CommandLine commandLine = new CommandLine(spyListCommand);
        commandLine.execute("--formats");
        Mockito.verify(spyListCommand, Mockito.times(1)).listFormats();
        commandLine.execute("--formats", "-j");
        Mockito.verify(spyListCommand, Mockito.times(2)).listFormats();
    }

    @Test
    void shouldListMutators() {
        ListCommand spyListCommand = Mockito.spy(listCommand);
        CommandLine commandLine = new CommandLine(spyListCommand);
        commandLine.execute("--mutators");
        Mockito.verify(spyListCommand, Mockito.times(1)).listMutators();
        commandLine.execute("--mutators", "-j");
        Mockito.verify(spyListCommand, Mockito.times(2)).listMutators();
    }

    @Test
    void shouldListMutatorsTypes() {
        ListCommand spyListCommand = Mockito.spy(listCommand);
        CommandLine commandLine = new CommandLine(spyListCommand);
        commandLine.execute("--cmt");
        Mockito.verify(spyListCommand, Mockito.times(1)).listMutatorsTypes();
        commandLine.execute("--cmt", "-j");
        Mockito.verify(spyListCommand, Mockito.times(2)).listMutatorsTypes();
    }

    @Test
    void shouldNotListFormats() {
        ListCommand spyListCommand = Mockito.spy(listCommand);
        CommandLine commandLine = new CommandLine(spyListCommand);
        commandLine.execute("--formats=false");
        Mockito.verify(spyListCommand, Mockito.times(0)).listFormats();
    }

    @Test
    void shouldListPlaybooksJson() {
        ListCommand spyListCommand = Mockito.spy(listCommand);
        CommandLine commandLine = new CommandLine(spyListCommand);
        commandLine.execute("-f", "-j");
        Mockito.verify(spyListCommand, Mockito.times(1)).listPlaybooks();
        Mockito.verify(spyListCommand, Mockito.times(0)).displayPlaybooks(Mockito.anyList(), Mockito.any());
    }

    @Test
    void shouldListPathsJson() {
        ListCommand spyListCommand = Mockito.spy(listCommand);
        CommandLine commandLine = new CommandLine(spyListCommand);
        commandLine.execute("-p", "-j", "-c", "src/test/resources/openapi.yml");
        Mockito.verify(spyListCommand, Mockito.times(1)).listContractPaths();
    }

    @Test
    void shouldListPathDetails() {
        ListCommand spyListCommand = Mockito.spy(listCommand);
        PrettyLogger spyLogger = Mockito.spy(PrettyLogger.class);
        ReflectionTestUtils.setField(spyListCommand, "logger", spyLogger);
        CommandLine commandLine = new CommandLine(spyListCommand);
        commandLine.execute("-p", "-j", "-c", "src/test/resources/openapi.yml", "--path", "/pet");
        Mockito.verify(spyListCommand, Mockito.times(1)).listPath(Mockito.any(), Mockito.eq("/pet"));
        Mockito.verify(spyLogger, Mockito.times(1)).noFormat(Mockito.any());

        Mockito.reset(spyListCommand, spyLogger);
        commandLine.execute("-p", "-c", "src/test/resources/openapi.yml", "--path", "/pet");
        Mockito.verify(spyListCommand, Mockito.times(1)).listPath(Mockito.any(), Mockito.eq("/pet"));
        Mockito.verify(spyLogger, Mockito.times(19)).noFormat(Mockito.any());

    }

    @Test
    void shouldListPlaybooks() {
        ListCommand spyListCommand = Mockito.spy(listCommand);
        CommandLine commandLine = new CommandLine(spyListCommand);
        commandLine.execute("-f");
        Mockito.verify(spyListCommand, Mockito.times(1)).listPlaybooks();
    }

    @Test
    void shouldNotListPlaybooks() {
        ListCommand spyListCommand = Mockito.spy(listCommand);
        CommandLine commandLine = new CommandLine(spyListCommand);
        commandLine.execute("-f=false");
        Mockito.verify(spyListCommand, Mockito.times(0)).listPlaybooks();
    }

    @Test
    void shouldListPlaybooksStrategies() {
        ListCommand spyListCommand = Mockito.spy(listCommand);
        CommandLine commandLine = new CommandLine(spyListCommand);
        commandLine.execute("-s");
        Mockito.verify(spyListCommand, Mockito.times(1)).listPlaybookStrategies();
    }

    @Test
    void shouldNotListPlaybooksStrategies() {
        ListCommand spyListCommand = Mockito.spy(listCommand);
        CommandLine commandLine = new CommandLine(spyListCommand);
        commandLine.execute("-s=false");
        Mockito.verify(spyListCommand, Mockito.times(0)).listPlaybookStrategies();
    }

    @ParameterizedTest
    @CsvSource({"-p,-c,src/test/resources/openapi.yml,1", "-p=false,-c,src/test/resources/openapi.yml,0", "-p,-c,openapi.yml,1"})
    void shouldListContractPaths(String arg1, String arg2, String arg3, int times) {
        ListCommand spyListCommand = Mockito.spy(listCommand);
        CommandLine commandLine = new CommandLine(spyListCommand);
        commandLine.execute(arg1, arg2, arg3);
        Mockito.verify(spyListCommand, Mockito.times(times)).listContractPaths();
    }

    @Test
    void shouldNotList() {
        ListCommand spyListCommand = Mockito.spy(listCommand);
        CommandLine commandLine = new CommandLine(spyListCommand);
        commandLine.execute();
        Mockito.verifyNoInteractions(spyListCommand);
    }
}
