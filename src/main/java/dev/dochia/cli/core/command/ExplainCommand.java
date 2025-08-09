package dev.dochia.cli.core.command;

import dev.dochia.cli.core.command.model.HelpFullOption;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.special.mutators.api.Mutator;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.ResultFactory;
import dev.dochia.cli.core.util.VersionProvider;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

@CommandLine.Command(
        name = "explain",
        mixinStandardHelpOptions = true,
        usageHelpAutoWidth = true,
        exitCodeOnInvalidInput = 191,
        exitCodeOnExecutionException = 192,
        exitCodeListHeading = "%n@|bold,underline Exit Codes:|@%n",
        exitCodeList = {"@|bold  0|@:Successful program execution",
                "@|bold 191|@:Usage error: user input for the command was incorrect",
                "@|bold 192|@:Internal execution error: an exception occurred when executing command"},
        footerHeading = "%n@|bold,underline Examples:|@%n",
        footer = {" Explain a 9XX response code:",
                "    dochia explain --type response_code 953",
                "", " Get more information about an error reason:",
                "    dochia explain --type error_reason \"Error details leak\"",},
        description = "Get detailed information about a playbook, mutator, response code or error reason.",
        versionProvider = VersionProvider.class)
@Unremovable
public class ExplainCommand implements Runnable {
    private final PrettyLogger logger = PrettyLoggerFactory.getConsoleLogger();

    @CommandLine.Option(names = {"-t", "--type"},
            description = "Output to console in JSON format.", required = true)
    Type type;

    @CommandLine.Mixin
    HelpFullOption helpFullOption;

    @CommandLine.Parameters(index = "0",
            paramLabel = "<info>",
            description = "The information you want to get details about.")
    String info;

    @Inject
    Instance<TestCasePlaybook> playbooks;

    @Inject
    Instance<Mutator> mutators;


    @Override
    public void run() {
        switch (type) {
            case PLAYBOOK -> displayPlaybookInfo();
            case MUTATOR -> displayMutatorInfo();
            case RESPONSE_CODE -> displayResponseCodeInfo();
            case ERROR_REASON -> displayErrorReason();
        }
    }

    void displayErrorReason() {
        Arrays.stream(ResultFactory.Reason.values()).filter(reason -> reason.name()
                        .toLowerCase(Locale.ROOT).contains(info.toLowerCase(Locale.ROOT)))
                .sorted()
                .forEach(reason -> logger.noFormat("* Reason {} - {}", reason.value(), reason.description()));
    }

    void displayPlaybookInfo() {
        playbooks.stream().filter(playbook -> playbook.getClass().getSimpleName()
                        .toLowerCase(Locale.ROOT).contains(info.toLowerCase(Locale.ROOT)))
                .sorted(Comparator.comparing(playbook -> playbook.getClass().getSimpleName()))
                .forEach(playbook -> logger.noFormat("* Playbook {} - {}", playbook.getClass().getSimpleName(), playbook.description()));
    }

    void displayMutatorInfo() {
        mutators.stream().filter(mutator -> mutator.getClass().getSimpleName()
                        .toLowerCase(Locale.ROOT).contains(info.toLowerCase(Locale.ROOT)))
                .sorted()
                .forEach(mutator -> logger.noFormat("* Mutator {} - {}", mutator.getClass().getSimpleName(), mutator.description()));
    }

    void displayResponseCodeInfo() {
        Arrays.stream(HttpResponse.ExceptionalResponse.values())
                .map(HttpResponse.ExceptionalResponse::asString)
                .filter(response -> response.toLowerCase(Locale.ROOT).contains(info.toLowerCase(Locale.ROOT)))
                .toList().stream().sorted()
                .forEach(logger::noFormat);
    }

    public enum Type {
        PLAYBOOK,
        MUTATOR,
        RESPONSE_CODE,
        ERROR_REASON
    }
}
