package dev.dochia.cli.core.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Entity holding details about time execution details for test cases.
 */
@Builder
@Getter
@ToString
public class TimeExecutionDetails {
    private final String path;
    private final List<TimeExecution> executions;
    private final TimeExecution bestCase;
    private final TimeExecution worstCase;
    private final double average;
}