package dev.dochia.cli.core.command.model;

import jakarta.inject.Singleton;
import picocli.CommandLine;

import java.io.File;

@Singleton
public class ConfigOptions {
    @CommandLine.Option(names = "--config-file", description = "Path to config file", paramLabel = "<file>")
    public File configFile;
}
