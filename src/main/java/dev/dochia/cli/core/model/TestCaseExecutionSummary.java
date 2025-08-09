package dev.dochia.cli.core.model;

/**
 * Used to hold only details about execution.
 *
 * @param path the service path
 * @param httpMethod the http method
 * @param responseTimeInMs the response time in ms
 */
public record TestCaseExecutionSummary(
    String testId, String path, String httpMethod, long responseTimeInMs) {}
