package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.util.ConsoleUtils;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Playbook that sends non rest HTTP methods, typically specific to WebDAV.
 */
@Singleton
@BodyPlaybook
public class NonRestHttpMethodsPlaybook implements TestCasePlaybook {
    private final List<String> fuzzedPaths = new ArrayList<>();
    private final HttpMethodPlaybookUtil httpMethodPlaybookUtil;

    /**
     * Creates a new NonRestHttpMethodsPlaybook instance.
     *
     * @param hmfu the utility class used to execute the logic
     */
    public NonRestHttpMethodsPlaybook(HttpMethodPlaybookUtil hmfu) {
        this.httpMethodPlaybookUtil = hmfu;
    }

    @Override
    public void run(PlaybookData data) {
        if (!fuzzedPaths.contains(data.getPath())) {
            for (HttpMethod httpMethod : HttpMethod.nonRestMethods()) {
                httpMethodPlaybookUtil.process(this, data, httpMethod);
            }
            fuzzedPaths.add(data.getPath());
        }
    }

    @Override
    public String description() {
        return "iterate through a list of HTTP methods specific to the WebDAV protocol that are not expected to be implemented by REST APIs";
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }
}
