package com.carddemo.batch.orchestration;

import java.nio.file.Path;
import java.util.Map;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

/**
 * Launches an orchestration pipeline by its short {@link BatchPipeline} name using the Spring
 * Batch {@link JobLauncher}.
 *
 * <p>Every launch gets a unique {@code run.id} identifying parameter so the same pipeline can be
 * re-run repeatedly (a fresh {@code JobInstance} each time). Report/interest job parameters
 * ({@code startDate}, {@code endDate}, {@code outputFile}, {@code parmDate}) are seeded from
 * caller-supplied values or the {@link BatchOrchestrationProperties} defaults; the underlying
 * CS-10/11/12 steps read them exactly as when launched individually.</p>
 */
@Service
public class BatchJobLauncherService {

    private static final Logger log = LoggerFactory.getLogger(BatchJobLauncherService.class);

    static final String PARAM_RUN_ID = "run.id";
    static final String PARAM_START_DATE = "startDate";
    static final String PARAM_END_DATE = "endDate";
    static final String PARAM_OUTPUT_FILE = "outputFile";
    static final String PARAM_PARM_DATE = "parmDate";

    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobsByBeanName;
    private final BatchOrchestrationProperties properties;

    public BatchJobLauncherService(JobLauncher jobLauncher,
                                   Map<String, Job> jobsByBeanName,
                                   BatchOrchestrationProperties properties) {
        this.jobLauncher = jobLauncher;
        this.jobsByBeanName = jobsByBeanName;
        this.properties = properties;
    }

    /** Launches the pipeline resolved from {@code name}, with no caller-supplied parameters. */
    public BatchJobLaunchResult launch(String name) {
        return launch(name, Map.of());
    }

    /**
     * Launches the pipeline resolved from {@code name}.
     *
     * @param name   short pipeline name (see {@link BatchPipeline})
     * @param params optional overrides for {@code startDate}/{@code endDate}/{@code outputFile}/
     *               {@code parmDate}; unknown keys are passed through as string parameters
     * @throws NoSuchElementException if {@code name} is not a known pipeline
     */
    public BatchJobLaunchResult launch(String name, Map<String, String> params) {
        BatchPipeline pipeline = BatchPipeline.fromName(name)
                .orElseThrow(() -> new NoSuchElementException("Unknown batch pipeline: " + name));
        Job job = jobsByBeanName.get(pipeline.getJobBeanName());
        if (job == null) {
            throw new IllegalStateException("Pipeline job bean not found: " + pipeline.getJobBeanName());
        }

        JobParametersBuilder builder = new JobParametersBuilder()
                .addLong(PARAM_RUN_ID, System.nanoTime(), true)
                .addString(PARAM_START_DATE, value(params, PARAM_START_DATE, properties.getDefaultReportStartDate()), false)
                .addString(PARAM_END_DATE, value(params, PARAM_END_DATE, properties.getDefaultReportEndDate()), false)
                .addString(PARAM_OUTPUT_FILE, value(params, PARAM_OUTPUT_FILE, defaultOutputFile(pipeline)), false)
                .addString(PARAM_PARM_DATE, value(params, PARAM_PARM_DATE, java.time.LocalDate.now().toString()), false);

        if (params != null) {
            params.forEach((k, v) -> {
                if (!isReservedParam(k)) {
                    builder.addString(k, v, false);
                }
            });
        }

        try {
            log.info("Launching batch pipeline '{}' (job {} / legacy {}).",
                    pipeline.getName(), pipeline.getJobBeanName(), pipeline.getLegacyJcl());
            JobExecution execution = jobLauncher.run(job, builder.toJobParameters());
            log.info("Batch pipeline '{}' finished with status {} (executionId={}).",
                    pipeline.getName(), execution.getStatus(), execution.getId());
            return new BatchJobLaunchResult(pipeline, execution);
        } catch (Exception e) {
            throw new BatchLaunchException("Failed to launch batch pipeline: " + pipeline.getName(), e);
        }
    }

    private String defaultOutputFile(BatchPipeline pipeline) {
        return Path.of(properties.getReportDir(), pipeline.getName() + "-report.txt").toString();
    }

    private static String value(Map<String, String> params, String key, String fallback) {
        if (params != null && params.get(key) != null && !params.get(key).isBlank()) {
            return params.get(key);
        }
        return fallback;
    }

    private static boolean isReservedParam(String key) {
        return PARAM_RUN_ID.equals(key)
                || PARAM_START_DATE.equals(key)
                || PARAM_END_DATE.equals(key)
                || PARAM_OUTPUT_FILE.equals(key)
                || PARAM_PARM_DATE.equals(key);
    }

    /** Thrown when a pipeline launch fails; unwraps the Spring Batch launch checked exceptions. */
    public static class BatchLaunchException extends RuntimeException {
        public BatchLaunchException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
