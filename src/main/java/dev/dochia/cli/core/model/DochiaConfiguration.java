package dev.dochia.cli.core.model;

import dev.dochia.cli.core.http.HttpMethod;
import java.util.List;

/**
 * This class is used to store the configuration context for the application.
 */
public record DochiaConfiguration(String version, String contract, String basePath,
                                  List<HttpMethod> httpMethods, int playbooks, long totalPlaybooks, int pathsToRun,
                                  int totalPaths) {
}
