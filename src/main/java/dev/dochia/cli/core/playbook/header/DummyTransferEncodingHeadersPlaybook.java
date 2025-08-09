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
 * Sends dummy Transfer-Encoding headers.
 */
@Singleton
@HeaderPlaybook
public class DummyTransferEncodingHeadersPlaybook extends BaseSecurityChecksHeadersPlaybook {
    private static final String DUMMY_TRANSFER = "dochia";

    /**
     * Creates a new instance.
     *
     * @param simpleExecutor executor used to run the fuzz logic.
     */
    public DummyTransferEncodingHeadersPlaybook(SimpleExecutor simpleExecutor) {
        super(simpleExecutor);
    }

    @Override
    public String getExpectedResponseCode() {
        return "400|501";
    }

    @Override
    public String typeOfHeader() {
        return "dummy";
    }

    @Override
    public ResponseCodeFamily getResponseCodeFamily() {
        return ResponseCodeFamilyPredefined.FOUR00_FIVE01;
    }

    @Override
    protected boolean shouldMatchContentType() {
        return false;
    }

    @Override
    public String targetHeaderName() {
        return HttpHeaders.TRANSFER_ENCODING;
    }

    @Override
    public List<Set<DochiaHeader>> getHeaders(PlaybookData data) {
        Set<DochiaHeader> clonedHeaders = Cloner.cloneMe(data.getHeaders());
        clonedHeaders.add(DochiaHeader.builder().name(HttpHeaders.TRANSFER_ENCODING).value(DUMMY_TRANSFER).build());

        return Collections.singletonList(clonedHeaders);
    }
}