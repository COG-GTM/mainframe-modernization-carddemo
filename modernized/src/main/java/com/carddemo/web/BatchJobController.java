package com.carddemo.web;

import com.carddemo.web.dto.JobExecutionResponse;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Replaces JCL job submission: runs a Spring Batch job on demand. */
@RestController
@RequestMapping("/api/v1/batch")
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobs;

    public BatchJobController(JobLauncher jobLauncher, Map<String, Job> jobs) {
        this.jobLauncher = jobLauncher;
        this.jobs = jobs;
    }

    @PostMapping("/{jobName}")
    public JobExecutionResponse run(@PathVariable String jobName,
                                    @RequestBody(required = false) Map<String, String> parameters) throws Exception {
        Job job = jobs.get(jobName);
        if (job == null) {
            throw new com.carddemo.exception.RecordNotFoundException("Unknown job: " + jobName);
        }
        JobParametersBuilder builder = new JobParametersBuilder()
                .addLong("runAt", System.currentTimeMillis());
        if (parameters != null) {
            parameters.forEach(builder::addString);
        }
        JobParameters jobParameters = builder.toJobParameters();
        JobExecution execution = jobLauncher.run(job, jobParameters);
        return new JobExecutionResponse(jobName, execution.getId(), execution.getStatus().toString(),
                execution.getExitStatus().getExitCode(), null);
    }
}
