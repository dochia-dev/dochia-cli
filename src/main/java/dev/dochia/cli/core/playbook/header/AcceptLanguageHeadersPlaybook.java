package dev.dochia.cli.core.playbook.header;

import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.header.base.BaseSecurityChecksHeadersPlaybook;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.PlaybookData;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
@HeaderPlaybook
public class AcceptLanguageHeadersPlaybook extends BaseSecurityChecksHeadersPlaybook {

    static final List<String> ACCEPT_LANGUAGE_VALUES = List.of("fr", "zh-CN", "ar-SA", "en-GB", "tlh-KL");

    public AcceptLanguageHeadersPlaybook(SimpleExecutor simpleExecutor) {
        super(simpleExecutor);
    }

    @Override
    public List<Set<DochiaHeader>> getHeaders(PlaybookData data) {
        List<Set<DochiaHeader>> headersToSend = new ArrayList<>();

        for (String value : ACCEPT_LANGUAGE_VALUES) {
            Set<DochiaHeader> headers = new HashSet<>(data.getHeaders());
            headers.add(DochiaHeader.builder().name("Accept-Language").value(value).build());
            headersToSend.add(headers);
        }

        return headersToSend;
    }

    @Override
    public ResponseCodeFamily getResponseCodeFamily() {
        return ResponseCodeFamilyPredefined.TWOXX;
    }

    @Override
    public String getExpectedResponseCode() {
        return "200";
    }

    @Override
    public String typeOfHeader() {
        return "locale";
    }

    @Override
    public String targetHeaderName() {
        return "Accept-Language";
    }

}
