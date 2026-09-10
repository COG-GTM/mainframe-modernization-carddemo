package com.carddemo.batch.readers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

/**
 * COBOL programs: CBACT01C, CBACT02C, CBACT03C, CBCUS01C — replacement for submitting
 * {@code READACCT}, {@code READCARD}, {@code READXREF} and {@code READCUST} JCL.
 *
 * <p>Jobs are not started automatically ({@code spring.batch.job.enabled=false}); this
 * component launches one by name, the way the JCL {@code EXEC PGM=} step does.
 */
@Component
public class SequentialReaderJobLauncher {

    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobs;

    public SequentialReaderJobLauncher(JobLauncher jobLauncher, List<Job> jobs) {
        this.jobLauncher = jobLauncher;
        this.jobs = jobs.stream().collect(LinkedHashMap::new,
                (map, job) -> map.put(job.getName(), job), Map::putAll);
    }

    /** Names of the jobs that can be launched. */
    public Set<String> jobNames() {
        return jobs.keySet();
    }

    /** Runs the job with the given name; a unique run id keeps every submission distinct. */
    public JobExecution launch(String jobName) throws Exception {
        return launch(jobName, new JobParametersBuilder()
                .addString("run.id", UUID.randomUUID().toString())
                .toJobParameters());
    }

    /** Runs the job with the given name and the given parameters. */
    public JobExecution launch(String jobName, JobParameters parameters) throws Exception {
        Job job = jobs.get(jobName);
        if (job == null) {
            throw new IllegalArgumentException("Unknown batch job: " + jobName
                    + " (known jobs: " + String.join(", ", jobs.keySet()) + ")");
        }
        return jobLauncher.run(job, parameters);
    }
}
