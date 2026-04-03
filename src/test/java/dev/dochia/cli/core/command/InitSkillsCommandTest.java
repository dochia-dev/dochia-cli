package dev.dochia.cli.core.command;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class InitSkillsCommandTest {

    @Test
    void shouldGenerateAllSkillFiles(@TempDir Path tempDir) {
        CommandLine commandLine = new CommandLine(new InitSkillsCommand());
        int exitCode = commandLine.execute("--dir", tempDir.toString());

        assertThat(exitCode).isZero();
        assertThat(tempDir.resolve("AGENTS.md")).doesNotExist();
        assertThat(tempDir.resolve(".agents/skills/dochia-test/SKILL.md")).exists();
        assertThat(tempDir.resolve(".agents/skills/dochia-fuzz/SKILL.md")).exists();
        assertThat(tempDir.resolve(".agents/skills/dochia-replay/SKILL.md")).exists();
        assertThat(tempDir.resolve(".agents/skills/dochia-list/SKILL.md")).exists();
        assertThat(tempDir.resolve(".agents/skills/dochia-explain/SKILL.md")).exists();
        assertThat(tempDir.resolve(".agents/skills/dochia-test/references/report-output.md")).exists();
    }

    @Test
    void shouldNotOverwriteExistingFilesWithoutForce(@TempDir Path tempDir) throws Exception {
        Path skillFile = tempDir.resolve(".agents/skills/dochia-test/SKILL.md");
        Files.createDirectories(skillFile.getParent());
        Files.writeString(skillFile, "existing content");

        CommandLine commandLine = new CommandLine(new InitSkillsCommand());
        int exitCode = commandLine.execute("--dir", tempDir.toString());

        assertThat(exitCode).isZero();
        assertThat(Files.readString(skillFile)).isEqualTo("existing content");
        assertThat(tempDir.resolve(".agents/skills/dochia-fuzz/SKILL.md")).exists();
    }

    @Test
    void shouldOverwriteExistingFilesWithForce(@TempDir Path tempDir) throws Exception {
        Path skillFile = tempDir.resolve(".agents/skills/dochia-test/SKILL.md");
        Files.createDirectories(skillFile.getParent());
        Files.writeString(skillFile, "existing content");

        CommandLine commandLine = new CommandLine(new InitSkillsCommand());
        int exitCode = commandLine.execute("--dir", tempDir.toString(), "--force");

        assertThat(exitCode).isZero();
        assertThat(Files.readString(skillFile))
                .isNotEqualTo("existing content")
                .contains("dochia-test");
    }

    @Test
    void shouldOverwriteExistingFilesWithShortForceFlag(@TempDir Path tempDir) throws Exception {
        Path skillFile = tempDir.resolve(".agents/skills/dochia-test/SKILL.md");
        Files.createDirectories(skillFile.getParent());
        Files.writeString(skillFile, "existing content");

        CommandLine commandLine = new CommandLine(new InitSkillsCommand());
        int exitCode = commandLine.execute("--dir", tempDir.toString(), "-f");

        assertThat(exitCode).isZero();
        assertThat(Files.readString(skillFile))
                .isNotEqualTo("existing content")
                .contains("dochia-test");
    }

    @Test
    void shouldContainValidSkillFrontmatter(@TempDir Path tempDir) throws Exception {
        CommandLine commandLine = new CommandLine(new InitSkillsCommand());
        commandLine.execute("--dir", tempDir.toString());

        String testSkill = Files.readString(tempDir.resolve(".agents/skills/dochia-test/SKILL.md"));
        assertThat(testSkill)
                .startsWith("---")
                .contains("name: dochia-test")
                .contains("description:")
                .contains("metadata:")
                .contains("triggers:")
                .contains("examples:");

        String fuzzSkill = Files.readString(tempDir.resolve(".agents/skills/dochia-fuzz/SKILL.md"));
        assertThat(fuzzSkill)
                .contains("name: dochia-fuzz")
                .contains("metadata:");
    }

    @Test
    void shouldReadResources() {
        InitSkillsCommand command = new InitSkillsCommand();
        String content = command.readResource("skills/dochia-test/SKILL.md");

        assertThat(content).isNotNull().contains("dochia-test");
    }

    @Test
    void shouldReturnNullForNonexistentResource() {
        InitSkillsCommand command = new InitSkillsCommand();
        String content = command.readResource("nonexistent/resource.md");

        assertThat(content).isNull();
    }

    @Test
    void shouldReturnFalseWhenResourceNotFound(@TempDir Path tempDir) throws IOException {
        InitSkillsCommand command = new InitSkillsCommand();
        Path target = tempDir.resolve("nonexistent/SKILL.md");

        boolean result = command.writeResourceFile("nonexistent/resource.md", target);

        assertThat(result).isFalse();
        assertThat(target).doesNotExist();
    }

    @Test
    void shouldReturnZeroExitCodeOnSuccess(@TempDir Path tempDir) {
        InitSkillsCommand command = new InitSkillsCommand();
        CommandLine commandLine = new CommandLine(command);
        commandLine.execute("--dir", tempDir.toString());

        assertThat(command.getExitCode()).isZero();
    }

    @Test
    void shouldUseShortDirFlag(@TempDir Path tempDir) {
        CommandLine commandLine = new CommandLine(new InitSkillsCommand());
        int exitCode = commandLine.execute("-d", tempDir.toString());

        assertThat(exitCode).isZero();
        assertThat(tempDir.resolve(".agents/skills/dochia-test/SKILL.md")).exists();
    }

    @Test
    void shouldBeIdempotentWhenRunTwiceWithForce(@TempDir Path tempDir) throws Exception {
        CommandLine commandLine1 = new CommandLine(new InitSkillsCommand());
        commandLine1.execute("--dir", tempDir.toString());

        String firstRun = Files.readString(tempDir.resolve(".agents/skills/dochia-test/SKILL.md"));

        CommandLine commandLine2 = new CommandLine(new InitSkillsCommand());
        commandLine2.execute("--dir", tempDir.toString(), "--force");

        String secondRun = Files.readString(tempDir.resolve(".agents/skills/dochia-test/SKILL.md"));

        assertThat(secondRun).isEqualTo(firstRun);
    }
}
