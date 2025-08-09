package dev.dochia.cli.core.playbook.api;

import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.model.PlaybookData;

import java.util.Collections;
import java.util.List;

/**
 * A Playbook is the main logic unit of <b>dochia</b>. Each Playbook will try to be run against each field
 * from a given request payload or query params. There are certain conditions which may prevent a
 * Playbook from running like data types for example. A Playbook targeting boolean fields should not be
 * run against a date field. <br>
 * Implement this in order to provide concrete fuzzing implementations.
 */
public interface TestCasePlaybook {

    /**
     * The actual execution logic of the Playbook.
     *
     * @param data the data constructed by the {@code dev.dochia.cli.core.factory.FuzzingDataFactory}
     */
    void run(PlaybookData data);

    /**
     * Simple description about the logic of the Playbook. This is displayed in the console when using {@code --help}
     *
     * @return description
     */
    String description();

    /**
     * Provides a list of HTTP methods that the implementing Playbook will not apply to.
     *
     * @return a list of HTTP methods the Playbook will skip running
     */
    default List<HttpMethod> skipForHttpMethods() {
        return Collections.emptyList();
    }

    /**
     * Provides a list of payload fields that the implementing Playbook will not apply to.
     *
     * @return a list of payload fields
     */
    default List<String> skipForFields() {
        return Collections.emptyList();
    }
}
