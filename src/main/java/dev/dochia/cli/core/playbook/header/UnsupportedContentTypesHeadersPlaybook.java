package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.util.JsonUtils;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import com.google.common.net.HttpHeaders;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Adds unsupported Content-Type headers.
 */
@Singleton
@HeaderPlaybook
public class UnsupportedContentTypesHeadersPlaybook extends DummyContentTypeHeadersPlaybook {

    /**
     * Creates a new instance.
     *
     * @param simpleExecutor executor used to run the fuzz logic
     */
    public UnsupportedContentTypesHeadersPlaybook(SimpleExecutor simpleExecutor) {
        super(simpleExecutor);
    }

    @Override
    public String typeOfHeader() {
        return "unsupported";
    }

    @Override
    public List<Set<DochiaHeader>> getHeaders(PlaybookData data) {
        if (JsonUtils.isEmptyPayload(data.getPayload())) {
            return Collections.emptyList();
        }
        return filterHeaders(data, HttpHeaders.CONTENT_TYPE, data.getRequestContentTypes());
    }

}
