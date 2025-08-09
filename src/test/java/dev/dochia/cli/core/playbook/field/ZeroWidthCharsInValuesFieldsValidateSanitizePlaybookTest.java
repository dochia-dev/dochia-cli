package dev.dochia.cli.core.playbook.field;

import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ZeroWidthCharsInValuesFieldsValidateSanitizePlaybookTest
 {

    @Test
    void shouldReturn4XX() {
        ZeroWidthCharsInValuesFieldsValidateSanitizePlaybook zwcf = new ZeroWidthCharsInValuesFieldsValidateSanitizePlaybook(null, null, null);
        Assertions.assertThat(zwcf.getExpectedHttpCodeWhenRequiredFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(zwcf.getExpectedHttpCodeWhenOptionalFieldsAreFuzzed()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
        Assertions.assertThat(zwcf.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.FOURXX);
    }
}
