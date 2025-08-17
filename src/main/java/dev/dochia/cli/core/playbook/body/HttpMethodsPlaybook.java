package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.util.ConsoleUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Playbook that sends undocumented HTTP methods.
 */
@Singleton
@BodyPlaybook
public class HttpMethodsPlaybook implements TestCasePlaybook {
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(HttpMethodsPlaybook.class);
    private final List<String> fuzzedPaths = new ArrayList<>();
    private final HttpMethodPlaybookUtil httpMethodPlaybookUtil;

    /**
     * Creates a new HttpMethodsPlaybook instance.
     *
     * @param hmfu the utility class used to execute the actual logic
     */
    @Inject
    public HttpMethodsPlaybook(HttpMethodPlaybookUtil hmfu) {
        this.httpMethodPlaybookUtil = hmfu;
    }

    @Override
    public void run(PlaybookData data) {
        if (!fuzzedPaths.contains(data.getPath())) {
            executeForOperation(data, PathItem::getPost, HttpMethod.POST);
            executeForOperation(data, PathItem::getPut, HttpMethod.PUT);
            executeForOperation(data, PathItem::getGet, HttpMethod.GET);
            executeForOperation(data, PathItem::getPatch, HttpMethod.PATCH);
            executeForOperation(data, PathItem::getDelete, HttpMethod.DELETE);
            executeForOperation(data, PathItem::getTrace, HttpMethod.TRACE);

            if (data.getPathItem().getGet() == null) {
                executeForOperation(data, PathItem::getHead, HttpMethod.HEAD);
            }
            fuzzedPaths.add(data.getPath());
        } else {
            logger.skip("Skip path {} as already fuzzed!", data.getPath());
        }
    }

    private void executeForOperation(PlaybookData data, Function<PathItem, Operation> operation, HttpMethod httpMethod) {
        if (operation.apply(data.getPathItem()) == null) {
            httpMethodPlaybookUtil.process(this, data, httpMethod);
        }
    }

    @Override
    public String toString() {
        return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
    }

    @Override
    public String description() {
        return "Iterate through each undocumented HTTP method and send empty request";
    }
}
