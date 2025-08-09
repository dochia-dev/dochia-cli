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
 * Playbook that sends hypothetical http methods that should not be part of a standard REST API.
 */
@Singleton
@BodyPlaybook
public class CustomHttpMethodsPlaybook implements TestCasePlaybook {
    private final List<String> fuzzedPaths = new ArrayList<>();
    private final HttpMethodPlaybookUtil httpMethodPlaybookUtil;


    /**
     * Creates a new NonRestHttpMethodsPlaybook instance.
     *
     * @param hmfu the utility class used to execute the logic
     */
    public CustomHttpMethodsPlaybook(HttpMethodPlaybookUtil hmfu) {
        this.httpMethodPlaybookUtil = hmfu;
    }

    @Override
    public void run(PlaybookData data) {
        if (!fuzzedPaths.contains(data.getPath())) {
            for (HttpMethod httpMethod : HttpMethod.hypotheticalMethods()) {
                httpMethodPlaybookUtil.process(this, data, httpMethod);
            }
            fuzzedPaths.add(data.getPath());
        }
    }

    @Override
    public String description() {
        return "iterate through a list of hypothetical HTTP methods that are not expected to be implemented by REST APIs";
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }
}