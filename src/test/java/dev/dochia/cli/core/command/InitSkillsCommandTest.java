package dev.dochia.cli.core.command;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class InitSkillsCommandTest {

    @Test
    void shouldGenerateAllSkillFiles(@TempDir Path tempDir) {
        CommandLine commandLine = new CommandLine(new InitSkillsCommand());
        int exitCode = commandLine.execute("--dir", tempDir.toString());

        assertEquals(0, exitCode);
        assertFalse(Files.exists(tempDir.resolve("AGENTS.md")));
        assertTrue(Files.exists(tempDir.resolve(".agents/skills/dochia-test/SKILL.md")));
        assertTrue(Files.exists(tempDir.resolve(".agents/skills/dochia-fuzz/SKILL.md")));
        assertTrue(Files.exists(tempDir.resolve(".agents/skills/dochia-replay/SKILL.md")));
        assertTrue(Files.exists(tempDir.resolve(".agents/skills/dochia-list/SKILL.md")));
        assertTrue(Files.exists(tempDir.resolve(".agents/skills/dochia-explain/SKILL.md")));
        assertTrue(Files.exists(tempDir.resolve(".agents/skills/dochia-test/references/report-output.md")));
    }

    @Test
    void shouldNotOverwriteExistingFilesWithoutForce(@TempDir Path tempDir) throws Exception {
        Path skillFile = tempDir.resolve(".agents/skills/dochia-test/SKILL.md");
        Files.createDirectories(skillFile.getParent());
        Files.writeString(skillFile, "existing content");

        CommandLine commandLine = new CommandLine(new InitSkillsCommand());
        int exitCode = commandLine.execute("--dir", tempDir.toString());

        assertEquals(0, exitCode);
        assertEquals("existing content", Files.readString(skillFile));
        // Other skills should still be created
        assertTrue(Files.exists(tempDir.resolve(".agents/skills/dochia-fuzz/SKILL.md")));
    }

    @Test
    void shouldOverwriteExistingFilesWithForce(@TempDir Path tempDir) throws Exception {
        Path skillFile = tempDir.resolve(".agents/skills/dochia-test/SKILL.md");
        Files.createDirectories(skillFile.getParent());
        Files.writeString(skillFile, "existing content");

        CommandLine commandLine = new CommandLine(new InitSkillsCommand());
        int exitCode = commandLine.execute("--dir", tempDir.toString(), "--force");

        assertEquals(0, exitCode);
        String content = Files.readString(skillFile);
        assertNotEquals("existing content", content);
        assertTrue(content.contains("dochia-test"));
    }

    @Test
    void shouldContainValidSkillFrontmatter(@TempDir Path tempDir) throws Exception {
        CommandLine commandLine = new CommandLine(new InitSkillsCommand());
        commandLine.execute("--dir", tempDir.toString());

        String testSkill = Files.readString(tempDir.resolve(".agents/skills/dochia-test/SKILL.md"));
        assertTrue(testSkill.startsWith("---"));
        assertTrue(testSkill.contains("name: dochia-test"));
        assertTrue(testSkill.contains("description:"));
        assertTrue(testSkill.contains("metadata:"));
        assertTrue(testSkill.contains("triggers:"));
        assertTrue(testSkill.contains("examples:"));

        String fuzzSkill = Files.readString(tempDir.resolve(".agents/skills/dochia-fuzz/SKILL.md"));
        assertTrue(fuzzSkill.contains("name: dochia-fuzz"));
        assertTrue(fuzzSkill.contains("metadata:"));
    }

    @Test
    void shouldReadResources() {
        InitSkillsCommand command = new InitSkillsCommand();
        String content = command.readResource("skills/dochia-test/SKILL.md");
        assertNotNull(content);
        assertTrue(content.contains("dochia-test"));
    }
}
