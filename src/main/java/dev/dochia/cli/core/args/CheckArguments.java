package dev.dochia.cli.core.args;

import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

/**
 * Holds all args related to category of Playbooks to run.
 */
@Singleton
@Getter
@Setter
public class CheckArguments {
    @CommandLine.Option(names = {"-A", "--check-headers"},
            description = "Run only Header ")
    private boolean checkHeaders;

    @CommandLine.Option(names = {"-F", "--check-fields"},
            description = "Run only Fields Test Case Playbooks")
    private boolean checkFields;

    @CommandLine.Option(names = {"-B", "--check-body"},
            description = "Run only Body Test Case Playbooks")
    private boolean checkHttp;

    @CommandLine.Option(names = {"-E", "--include-emojis"},
            description = "Include Emojis Test Case Playbooks. Default: @|bold,underline ${DEFAULT-VALUE}|@")
    private boolean includeEmojis = true;
}
