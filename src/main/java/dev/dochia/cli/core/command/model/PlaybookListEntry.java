package dev.dochia.cli.core.command.model;

import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents an entry in a playbook list, including a category and a list of playbook details.
 */
@Getter
public class PlaybookListEntry {
    private String category;
    private List<PlaybookDetails> playbooks = new ArrayList<>();

    /**
     * Sets the category of the playbook list entry.
     *
     * @param category the category to set
     * @return this {@code PlaybookListEntry} for method chaining
     */
    public PlaybookListEntry category(String category) {
        this.category = category;
        return this;
    }

    /**
     * Sets the list of playbook details based on the provided list of playbooks.
     *
     * @param playbooksList the list of playbooks to set details for
     * @return this {@code PlaybookListEntry} for method chaining
     */
    public PlaybookListEntry playbooks(List<TestCasePlaybook> playbooksList) {
        this.playbooks = playbooksList.stream()
                .map(playbook -> PlaybookDetails.builder()
                        .name(playbook.getClass().getSimpleName())
                        .description(playbook.description())
                        .build())
                .collect(Collectors.toList());
        return this;
    }

    /**
     * Represents details about a playbook, including its name and description.
     */
    @Builder
    public static class PlaybookDetails {
        private String name;
        private String description;
    }
}
