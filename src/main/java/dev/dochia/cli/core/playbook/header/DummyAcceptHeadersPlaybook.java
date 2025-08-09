package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.header.base.BaseSecurityChecksHeadersPlaybook;
import dev.dochia.cli.core.generator.Cloner;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import com.google.common.net.HttpHeaders;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Sends dummy Accept headers.
 */
@Singleton
@HeaderPlaybook
public class DummyAcceptHeadersPlaybook extends BaseSecurityChecksHeadersPlaybook {

    /**
     * Creates a new instance.
     *
     * @param simpleExecutor executor used to run the fuzz logic
     */
    public DummyAcceptHeadersPlaybook(SimpleExecutor simpleExecutor) {
        super(simpleExecutor);
    }

    @Override
    public ResponseCodeFamily getResponseCodeFamily() {
        return ResponseCodeFamilyPredefined.FOURXX_MT;
    }

    @Override
    public String getExpectedResponseCode() {
        return "406";
    }

    @Override
    public String typeOfHeader() {
        return "dummy";
    }

    @Override
    public String targetHeaderName() {
        return HttpHeaders.ACCEPT;
    }

    @Override
    public List<Set<DochiaHeader>> getHeaders(PlaybookData data) {
        Set<DochiaHeader> clonedHeaders = Cloner.cloneMe(data.getHeaders());
        clonedHeaders.add(DochiaHeader.builder().name(HttpHeaders.ACCEPT).value(DOCHIA_ACCEPT).build());

        return Collections.singletonList(clonedHeaders);
    }
}
