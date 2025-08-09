package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import com.google.common.net.HttpHeaders;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Adds unsupported Accept headers.
 */
@Singleton
@HeaderPlaybook
public class UnsupportedAcceptHeadersPlaybook extends DummyAcceptHeadersPlaybook {

    /**
     * Creates a new instance.
     *
     * @param simpleExecutor executor used to run the fuzz logic
     */
    public UnsupportedAcceptHeadersPlaybook(SimpleExecutor simpleExecutor) {
        super(simpleExecutor);
    }

    @Override
    public String typeOfHeader() {
        return "unsupported";
    }

    @Override
    public List<Set<DochiaHeader>> getHeaders(PlaybookData data) {
        return filterHeaders(data, HttpHeaders.ACCEPT, data.getResponseContentTypes().values().
                stream().flatMap(Collection::stream).toList());
    }
}
