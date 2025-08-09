package dev.dochia.cli.core.command.model;

import jakarta.inject.Singleton;
import picocli.CommandLine;

@Singleton
public class HelpFullOption {
    @CommandLine.Option(names = "--help-full", description = "Show full help and exit")
    boolean helpFull;
}
