package com.carddemo.batch;

import com.carddemo.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Runs the seed {@link #dataLoadJob} at startup when {@code carddemo.seed.enabled=true}.
 *
 * <p>Disabled by default so the application (and the smoke tests) boot without touching data.
 * Enable with {@code --carddemo.seed.enabled=true} (or the env var
 * {@code CARDDEMO_SEED_ENABLED=true}) to load the ASCII seed files. The load is idempotent:
 * it is skipped when accounts already exist.</p>
 */
@Component
@ConditionalOnProperty(name = "carddemo.seed.enabled", havingValue = "true")
public class SeedDataLoadRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLoadRunner.class);

    private final JobLauncher jobLauncher;
    private final Job dataLoadJob;
    private final AccountRepository accountRepository;

    public SeedDataLoadRunner(JobLauncher jobLauncher, Job dataLoadJob,
                              AccountRepository accountRepository) {
        this.jobLauncher = jobLauncher;
        this.dataLoadJob = dataLoadJob;
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (accountRepository.count() > 0) {
            log.info("Seed data already present ({} accounts); skipping dataLoadJob.",
                    accountRepository.count());
            return;
        }
        log.info("Launching dataLoadJob to load ASCII seed files.");
        jobLauncher.run(dataLoadJob, new JobParametersBuilder()
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters());
    }
}
