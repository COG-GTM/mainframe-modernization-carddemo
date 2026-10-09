package com.carddemo.web.dto;

/** Outcome of a Spring Batch job that replaces a JCL job. */
public record JobExecutionResponse(String jobName, Long executionId, String status, String exitCode, String output) {
}
