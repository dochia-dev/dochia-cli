package dev.dochia.cli.core.playbook.header.only;

import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class OnlySpacesInHeadersPlaybookTest {
    private OnlySpacesInHeadersPlaybook onlySpacesInHeadersPlaybook;

    @BeforeEach
    void setup() {
        onlySpacesInHeadersPlaybook = new OnlySpacesInHeadersPlaybook(null);
    }

    @Test
    void shouldHaveReplaceFuzzingStrategy() {
        Assertions.assertThat(onlySpacesInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo(FuzzingStrategy.replace().name());
        Assertions.assertThat(onlySpacesInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().getData()).isEqualTo(" ");
    }

    @Test
    void shouldReturn4xxForRequiredAnd2xxForOptionalResponseCodes() {
        Assertions.assertThat(onlySpacesInHeadersPlaybook.getPlaybookContext().getExpectedHttpCodeForRequiredHeadersFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(onlySpacesInHeadersPlaybook.getPlaybookContext().getExpectedHttpForOptionalHeadersFuzzed().apply(DochiaHeader.builder().build())).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
    }

    @Test
    void shouldReturn4xxAnd2xxForOptionalHeaders() {
        Parameter parameter = new Parameter();
        parameter.setSchema(new Schema().format("date"));
        DochiaHeader header = DochiaHeader.fromHeaderParameter(parameter);
        Assertions.assertThat(onlySpacesInHeadersPlaybook.getPlaybookContext().getExpectedHttpForOptionalHeadersFuzzed()
                .apply(header)).isEqualTo(ResponseCodeFamilyPredefined.FOURXX_TWOXX);
    }

    @Test
    void shouldHaveToString() {
        Assertions.assertThat(onlySpacesInHeadersPlaybook.description()).isNotBlank();
    }

    @Test
    void shouldMatchResponseSchema() {
        Assertions.assertThat(onlySpacesInHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isTrue();
    }

    @Test
    void shouldHaveTypeOfDataToSend() {
        Assertions.assertThat(onlySpacesInHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isNotBlank();
    }
}
