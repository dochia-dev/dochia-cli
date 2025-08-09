package dev.dochia.cli.core.playbook.header.leading;

import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class LeadingSpacesInHeadersPlaybookTest {
    private LeadingSpacesInHeadersPlaybook leadingSpacesInHeadersPlaybook;

    @BeforeEach
    void setup() {
        leadingSpacesInHeadersPlaybook = new LeadingSpacesInHeadersPlaybook(null);
    }

    @Test
    void shouldExpect2xxForOptionalAndRequiredHeaders() {
        Assertions.assertThat(leadingSpacesInHeadersPlaybook.getPlaybookContext().getExpectedHttpCodeForRequiredHeadersFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(leadingSpacesInHeadersPlaybook.getPlaybookContext().getExpectedHttpForOptionalHeadersFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
    }

    @Test
    void shouldMatchResponseSchema() {
        Assertions.assertThat(leadingSpacesInHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isTrue();
    }

    @Test
    void shouldReturnPrefixFuzzingStrategy() {
        Assertions.assertThat(leadingSpacesInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo(FuzzingStrategy.prefix().name());
        Assertions.assertThat(leadingSpacesInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().getData()).isEqualTo(" ");
        Assertions.assertThat(leadingSpacesInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().get(1).getData()).isEqualTo("\u0009");
        Assertions.assertThat(leadingSpacesInHeadersPlaybook.getPlaybookContext().getFuzzStrategy()).hasSize(2);

    }

    @Test
    void shouldHaveDescription() {
        Assertions.assertThat(leadingSpacesInHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldHaveTypeOfDataToSend() {
        Assertions.assertThat(leadingSpacesInHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isNotBlank();
    }
}
