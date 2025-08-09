package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.args.MatchArguments;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
class ZalgoTextInHeadersPlaybookTest {

    private ZalgoTextInHeadersPlaybook zalgoTextInHeadersPlaybook;

    @BeforeEach
    void setup() {
        ServiceCaller serviceCaller = Mockito.mock(ServiceCaller.class);
        TestCaseListener testCaseListener = Mockito.mock(TestCaseListener.class);
        HeadersIteratorExecutor headersIteratorExecutor = new HeadersIteratorExecutor(serviceCaller, testCaseListener, Mockito.mock(MatchArguments.class), Mockito.mock(FilterArguments.class));
        zalgoTextInHeadersPlaybook = new ZalgoTextInHeadersPlaybook(headersIteratorExecutor);
    }

    @Test
    void shouldHaveAllMethodsOverridden() {
        Assertions.assertThat(zalgoTextInHeadersPlaybook.description()).isNotNull();
        Assertions.assertThat(zalgoTextInHeadersPlaybook.getPlaybookContext().getTypeOfDataSentToTheService()).isNotNull();
        Assertions.assertThat(zalgoTextInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().name()).isEqualTo(FuzzingStrategy.replace().name());
        Assertions.assertThat(zalgoTextInHeadersPlaybook.getPlaybookContext().isMatchResponseSchema()).isFalse();
    }

    @Test
    void shouldGetZalgoTextAsPayload() {
        Assertions.assertThat(zalgoTextInHeadersPlaybook.getPlaybookContext().getFuzzStrategy()).hasSize(1);
        Assertions.assertThat(zalgoTextInHeadersPlaybook.getPlaybookContext().getFuzzStrategy().getFirst().getData()).isEqualTo(" ̵̡̡̢̡̨̨̢͚̬̱̤̰̗͉͚̖͙͎͔͔̺̳͕̫̬͚̹͖̬̭̖̪̗͕̜̣̥̣̼͍͉̖͍̪͈̖͚̙͛͒͂̎̊̿̀̅̈͌͋̃̾̈̾̇͛͌͘͜͜͠͝ͅͅͅ ̷͕̗̇͛̅̀̑̇̈͗͌͛̐̀͆̐̊̅̋̈́̂̈́̈́͑̓͂͂̌̈́̽͌͐̐͂͐̈́̍̂͗̂͘͠͝͝͝ͅ ̷̨̢̧̢̡̨̛͕̯̭̹͖̮̘̤̩̥̟̖͈̯̠̖͈̜͈̥̫͔̘̭͉͎͇̤̦̯͙̹̠̼̮͕̲̖̟̲̦̣͇̳͖̳̭͇͓̭͌̓̀̅̋̋̀̈́̎̄͛̾̊͐̎̉̏͊͐̑͊͒̐̔̏̔̋̑̌͆̏̀̉͆̆́̓̆̉̀̒̆̆̉̀̂̎̈̔͗̔̕̕͘̕̚̚̕͘͜͝͝͝͝͝͠ͅ ̷̧̡̥͈͓͙͈̫͙͎͈̻̔̊̎̏̑̒̐̐̆̉̍͠͝͝ ̴̡̛̛͓͎͇̘͈͇̱̟̠̳͇̬̺̲̭̪̬̼̝̠̙̹̩̱̪͔͉͎̱͚͍̬͈̤͈͙͖̝̲̦̞̺̟̟̺͇̳͈̠̘̺̪̱̮̉̀̍̏̐̃̅̐̊̾͆̐͋͊̿̉̆̾͊̀͊͒͌̀͛̎́́͂̐͂̎͛̆͜͜͜͠ͅ ̶̧̧͖̻̥̝̺̼̙̫̩̹̣̲̩̲͍̺̘͕̤͉̹̥͉̮̮̟̘̥̺̯̗̠͈̬͚̦̦͚̫̫̦̉́̾̀̅͋̋̇̕̕͜͜͝ͅͅ ̶̧̛̛̝̟̤̬̙͔̻͙͚̹̣̳̳͔̥̘̠̗̦̠͚͎̖̮̳̗̥̫͚̯̬̩̎́̽͒̋̓̀͂̈́̓́̎͐͊͒̎͒͌̿̿̔͐̈́͑̊̄̓̎͐̓̓̍͘̕̚̚͜͜ ̶̢̡̡̨̡̡̘̫̫̠̟̻̳̻͈̲̖͚͇̼̩̥̥͎̥̯͚̞̘̼̞͍̮̗͈̱͚͙̠͔̞̮̱̭͍͍̪̲̜͓͍̣̯̲̠̲̤̅͊̑̇̆́̈́̓̿̄̐̓̐͐́͛̆͜͝͝͝͠ͅ ̶̧̡̨̧̡̧̥̥̱̪͇̞̭͙͚͔̜̠͓͈̞͈̣̹̝̩̦̟̻̰͙̯̼̜̞̮̬̝͚̺̟͎̻̱̙̦̜̭̲̰͎̳̣̈͜͜͜ͅ ̸̹̟̯̝͚̪̼͓͕͕̹͖̣̠͓̫͇͚͔̼̊́͑̊̊̅͗͠ͅ".replace(" ", ""));
    }
}
