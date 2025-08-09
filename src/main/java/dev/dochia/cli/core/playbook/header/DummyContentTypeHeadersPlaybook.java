package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.header.base.BaseSecurityChecksHeadersPlaybook;
import dev.dochia.cli.core.generator.Cloner;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.util.JsonUtils;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import com.google.common.net.HttpHeaders;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Sends dummy Content-Type headers.
 */
@Singleton
@HeaderPlaybook
public class DummyContentTypeHeadersPlaybook extends BaseSecurityChecksHeadersPlaybook {

    /**
     * Creates a new instance.
     *
     * @param simpleExecutor executor used to run the fuzz logic
     */
    public DummyContentTypeHeadersPlaybook(SimpleExecutor simpleExecutor) {
        super(simpleExecutor);
    }

    @Override
    public String getExpectedResponseCode() {
        return "415";
    }

    @Override
    public String typeOfHeader() {
        return "dummy";
    }

    @Override
    public String targetHeaderName() {
        return HttpHeaders.CONTENT_TYPE;
    }

    @Override
    public ResponseCodeFamily getResponseCodeFamily() {
        return ResponseCodeFamilyPredefined.FOURXX_MT;
    }

    @Override
    public List<Set<DochiaHeader>> getHeaders(PlaybookData data) {
        if (JsonUtils.isEmptyPayload(data.getPayload())) {
            return Collections.emptyList();
        }
        Set<DochiaHeader> clonedHeaders = Cloner.cloneMe(data.getHeaders());
        clonedHeaders.add(DochiaHeader.builder().name(HttpHeaders.CONTENT_TYPE).value(DOCHIA_ACCEPT).build());

        return Collections.singletonList(clonedHeaders);
    }

    @Override
    public List<HttpMethod> skipForHttpMethods() {
        return Arrays.asList(HttpMethod.DELETE, HttpMethod.GET);
    }
}
