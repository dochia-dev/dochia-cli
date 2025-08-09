package dev.dochia.cli.core.playbook.field.base;

import dev.dochia.cli.core.playbook.api.TestCasePlaybook;

import java.util.List;

/**
 * Marker interface for playbooks using dochia dsl.
 */
public interface CustomPlaybookBase extends TestCasePlaybook {

    /**
     * Retrieves a list of reserved words.
     *
     * @return A {@link List} of {@link String} representing reserved words.
     */
    List<String> requiredKeywords();
}
