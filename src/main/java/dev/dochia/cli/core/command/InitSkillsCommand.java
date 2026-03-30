package dev.dochia.cli.core.command;

import dev.dochia.cli.core.util.VersionProvider;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.quarkus.arc.Unremovable;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Generates Agent Skills files for agentic IDE integration.
 * Skills are placed in .agents/skills/ following the open Agent Skills specification
 * (https://agentskills.io), which is supported by Windsurf, Cursor, Claude Code, and OpenAI Codex.
 */
@CommandLine.Command(
        name = "init-skills",
        mixinStandardHelpOptions = true,
        usageHelpAutoWidth = true,
        exitCodeListHeading = "%n@|bold,underline Exit Codes:|@%n",
        exitCodeList = {"@|bold  0|@:Successful program execution",
                "@|bold 2|@:Usage error: user input for the command was incorrect",
                "@|bold 1|@:Internal execution error: an exception occurred when executing command"},
        footerHeading = "%n@|bold,underline Examples:|@%n",
        footer = {"  Initialize agent skills in the current directory:",
                "    dochia init-skills",
                "", "  Initialize in a specific directory:",
                "    dochia init-skills --dir /path/to/project",
                "", "  Force overwrite existing files:",
                "    dochia init-skills --force"},
        description = "Generate Agent Skills for agentic IDE integration (Windsurf, Cursor, Claude Code, Codex).",
        versionProvider = VersionProvider.class)
@Unremovable
public class InitSkillsCommand implements Runnable, CommandLine.IExitCodeGenerator {
    private final PrettyLogger logger = PrettyLoggerFactory.getConsoleLogger();

    @CommandLine.Option(names = {"--dir", "-d"},
            description = "Target directory to generate skills in. Defaults to current directory.",
            defaultValue = ".")
    private String directory;

    @CommandLine.Option(names = {"--force", "-f"},
            description = "Overwrite existing files if they already exist.")
    private boolean force;

    private int exitCode;

    private static final List<String> SKILL_NAMES = List.of(
            "dochia-test",
            "dochia-fuzz",
            "dochia-replay",
            "dochia-list",
            "dochia-explain"
    );

    private static final Map<String, List<String>> SKILL_EXTRA_FILES = Map.of(
            "dochia-test", List.of("references/report-output.md")
    );

    @Override
    public void run() {
        try {
            Path baseDir = Path.of(directory).toAbsolutePath().normalize();
            logger.info("Generating Dochia agent skills in {}", baseDir);

            int created = 0;
            int skipped = 0;

            for (String skillName : SKILL_NAMES) {
                Path skillDir = baseDir.resolve(".agents").resolve("skills").resolve(skillName);
                Path skillFile = skillDir.resolve("SKILL.md");
                String resourcePath = "skills/" + skillName + "/SKILL.md";

                if (writeResourceFile(resourcePath, skillFile)) {
                    created++;
                } else {
                    skipped++;
                }

                for (String extraFile : SKILL_EXTRA_FILES.getOrDefault(skillName, List.of())) {
                    Path extraTarget = skillDir.resolve(extraFile);
                    String extraResource = "skills/" + skillName + "/" + extraFile;
                    if (writeResourceFile(extraResource, extraTarget)) {
                        created++;
                    } else {
                        skipped++;
                    }
                }
            }

            logger.noFormat("");
            logger.info("Done! {} files created, {} skipped (already exist).", created, skipped);

            if (created > 0) {
                logger.noFormat("");
                logger.info("Generated files:");
                for (String skillName : SKILL_NAMES) {
                    Path skillFile = baseDir.resolve(".agents").resolve("skills").resolve(skillName).resolve("SKILL.md");
                    if (Files.exists(skillFile)) {
                        logger.noFormat("  .agents/skills/{}/SKILL.md", skillName);
                    }
                }
                logger.noFormat("");
                logger.info("These files are automatically discovered by:");
                logger.noFormat("  - Windsurf (Cascade)");
                logger.noFormat("  - Cursor");
                logger.noFormat("  - Claude Code");
                logger.noFormat("  - OpenAI Codex");
                logger.noFormat("");
                logger.info("Commit them to your repository so your team benefits too.");
            }

            if (skipped > 0 && !force) {
                logger.note("Use --force to overwrite existing files.");
            }

            exitCode = 0;
        } catch (Exception e) {
            logger.error("Failed to generate agent skills: {}", e.getMessage());
            logger.debug("Stacktrace", e);
            exitCode = 1;
        }
    }

    /**
     * Writes a classpath resource to a target file path.
     *
     * @return true if the file was written, false if it was skipped
     */
    boolean writeResourceFile(String resourcePath, Path targetFile) throws IOException {
        if (Files.exists(targetFile) && !force) {
            logger.skip("  Skipping {} (already exists)", targetFile.toAbsolutePath().normalize());
            return false;
        }

        String content = readResource(resourcePath);
        if (content == null) {
            logger.error("  Resource not found: {}", resourcePath);
            return false;
        }

        Files.createDirectories(targetFile.getParent());
        Files.writeString(targetFile, content, StandardCharsets.UTF_8);
        logger.noFormat("  Created {}", targetFile.toAbsolutePath().normalize());
        return true;
    }

    String readResource(String resourcePath) {
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (in == null) {
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.debug("Error reading resource: {}", resourcePath, e);
            return null;
        }
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
