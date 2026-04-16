package com.cardemo.batch.orchestration.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuration for job status monitoring via Spring Boot Actuator.
 * Exposes batch job execution information at /actuator/batchjobs endpoint.
 *
 * <p>Provides:
 * <ul>
 *   <li>Current status of each orchestrated job flow</li>
 *   <li>Last execution timestamps</li>
 *   <li>Execution counts and failure rates</li>
 *   <li>Custom metrics for batch processing</li>
 * </ul>
 */
@Configuration
public class ActuatorConfig {

    /**
     * Custom Actuator endpoint for batch job status monitoring.
     * Accessible at /actuator/batchjobs
     */
    @Component
    @Endpoint(id = "batchjobs")
    public static class BatchJobsEndpoint {

        private final JobExplorer jobExplorer;
        private final MeterRegistry meterRegistry;

        public BatchJobsEndpoint(JobExplorer jobExplorer, MeterRegistry meterRegistry) {
            this.jobExplorer = jobExplorer;
            this.meterRegistry = meterRegistry;
        }

        @ReadOperation
        public Map<String, Object> batchJobStatus() {
            Map<String, Object> status = new LinkedHashMap<>();

            status.put("posttran", getJobInfo("posttranJob"));
            status.put("intcalc", getJobInfo("intcalcJob"));
            status.put("creastmt", getJobInfo("creastmtJob"));

            Map<String, Object> metrics = new HashMap<>();
            addMetric(metrics, "cardemo.batch.flow.started");
            addMetric(metrics, "cardemo.batch.flow.completed");
            status.put("metrics", metrics);

            return status;
        }

        private Map<String, Object> getJobInfo(String jobName) {
            Map<String, Object> info = new LinkedHashMap<>();

            var instances = jobExplorer.findJobInstancesByJobName(jobName, 0, 1);
            if (instances.isEmpty()) {
                info.put("status", "NEVER_RUN");
                info.put("lastExecution", null);
                return info;
            }

            var lastInstance = instances.get(0);
            var executions = jobExplorer.getJobExecutions(lastInstance);
            if (executions.isEmpty()) {
                info.put("status", "NO_EXECUTIONS");
                return info;
            }

            var lastExecution = executions.get(0);
            info.put("status", lastExecution.getStatus().toString());
            info.put("exitCode", lastExecution.getExitStatus().getExitCode());
            info.put("startTime", lastExecution.getStartTime() != null
                    ? lastExecution.getStartTime().toString() : null);
            info.put("endTime", lastExecution.getEndTime() != null
                    ? lastExecution.getEndTime().toString() : null);
            info.put("jobInstanceId", lastInstance.getInstanceId());
            info.put("jobExecutionId", lastExecution.getId());

            info.put("steps", lastExecution.getStepExecutions().stream()
                    .map(step -> {
                        Map<String, Object> stepInfo = new LinkedHashMap<>();
                        stepInfo.put("name", step.getStepName());
                        stepInfo.put("status", step.getStatus().toString());
                        stepInfo.put("exitCode", step.getExitStatus().getExitCode());
                        stepInfo.put("readCount", step.getReadCount());
                        stepInfo.put("writeCount", step.getWriteCount());
                        return stepInfo;
                    })
                    .collect(Collectors.toList()));

            return info;
        }

        private void addMetric(Map<String, Object> metrics, String metricName) {
            var counter = meterRegistry.find(metricName).counters();
            if (counter != null) {
                counter.forEach(c -> {
                    String key = metricName;
                    if (!c.getId().getTags().isEmpty()) {
                        key += c.getId().getTags().stream()
                                .map(t -> t.getKey() + "=" + t.getValue())
                                .collect(Collectors.joining(",", "[", "]"));
                    }
                    metrics.put(key, c.count());
                });
            }
        }
    }
}
