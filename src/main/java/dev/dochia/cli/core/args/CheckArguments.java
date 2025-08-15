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
    @CommandLine.Option(names = {"-A", "--headers-only"},
            description = "Run only Header Playbooks")
    private boolean checkHeaders;

    @CommandLine.Option(names = {"-F", "--fields-only"},
            description = "Run only Fields Playbooks")
    private boolean checkFields;

    @CommandLine.Option(names = {"-B", "--body-only"},
            description = "Run only Body Playbooks")
    private boolean checkHttp;

    @CommandLine.Option(names = {"-E", "--include-emojis"}, negatable = true, defaultValue = "true", fallbackValue = "true",
            description = "Include Emojis Test Case Playbooks. Default: @|bold,underline ${DEFAULT-VALUE}|@")
    private boolean includeEmojis = true;
}
