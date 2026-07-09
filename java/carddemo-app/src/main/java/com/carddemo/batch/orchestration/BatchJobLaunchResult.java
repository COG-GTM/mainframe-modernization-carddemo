package com.carddemo.batch.orchestration;

import org.springframework.batch.core.JobExecution;

/**
 * Immutable summary of a launched orchestration pipeline, returned by the REST endpoint and the
 * launcher service.
 */
public class BatchJobLaunchResult {

    private final String pipeline;
    private final String jobName;
    private final String legacyJcl;
    private final Long jobExecutionId;
    private final Long jobInstanceId;
    private final String status;
    private final String exitCode;
    private final String startTime;
    private final String endTime;

    public BatchJobLaunchResult(BatchPipeline pipeline, JobExecution execution) {
        this.pipeline = pipeline.getName();
        this.jobName = pipeline.getJobBeanName();
        this.legacyJcl = pipeline.getLegacyJcl();
        this.jobExecutionId = execution.getId();
        this.jobInstanceId = execution.getJobInstance() == null ? null : execution.getJobInstance().getInstanceId();
        this.status = execution.getStatus().toString();
        this.exitCode = execution.getExitStatus() == null ? null : execution.getExitStatus().getExitCode();
        this.startTime = execution.getStartTime() == null ? null : execution.getStartTime().toString();
        this.endTime = execution.getEndTime() == null ? null : execution.getEndTime().toString();
    }

    public String getPipeline() {
        return pipeline;
    }

    public String getJobName() {
        return jobName;
    }

    public String getLegacyJcl() {
        return legacyJcl;
    }

    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public Long getJobInstanceId() {
        return jobInstanceId;
    }

    public String getStatus() {
        return status;
    }

    public String getExitCode() {
        return exitCode;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
