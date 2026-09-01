package com.carddemo.posttran;

import com.carddemo.posttran.domain.DalytranRecord;
import com.carddemo.posttran.io.FixedWidthFiles;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.util.List;

/**
 * POSTTRAN as a Spring Batch job: the JCL job is the {@link Job}, {@code STEP15} is the
 * {@link Step}, and the DD statements are {@link DdPaths}.
 *
 * <p>The chunk size is 1 deliberately. CBTRN02C reads one daily transaction, validates it against
 * account state and immediately rewrites that state, so the next record sees the effect of the
 * previous one; any larger chunk would only be safe if the step were also rewritten to batch its
 * updates, and that would change results.
 *
 * <p>The job repository is resourceless — this step has no restart semantics to preserve (its
 * output GDG generation is created fresh on every run, POSTTRAN.jcl:33), so there is nothing worth
 * persisting and no reason to drag a database into a file-to-file batch step.
 */
@Configuration
@EnableConfigurationProperties(DdPaths.class)
public class PostTranJobConfig {

    @Bean
    public JobRepository jobRepository() {
        return new ResourcelessJobRepository();
    }

    @Bean
    public PlatformTransactionManager batchTransactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.afterPropertiesSet();
        return launcher;
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public Db2Timestamp db2Timestamp(Clock clock) {
        return new Db2Timestamp(clock);
    }

    @Bean
    public TransactionPoster transactionPoster(DdPaths dd, Db2Timestamp timestamp) {
        return new TransactionPoster(dd, timestamp);
    }

    @Bean
    public StepReturnCode stepReturnCode() {
        return new StepReturnCode();
    }

    /** {@code 1000-DALYTRAN-GET-NEXT} — sequential read of the 350-byte input, CBTRN02C.cbl:345. */
    @Bean
    public ItemReader<DalytranRecord> dalytranReader(DdPaths dd) {
        List<DalytranRecord> records = FixedWidthFiles.read(dd.dalytran(), DalytranRecord.LENGTH)
                .stream()
                .map(DalytranRecord::new)
                .toList();
        return new ListItemReader<>(records);
    }

    @Bean
    public ItemWriter<DalytranRecord> postTranWriter(TransactionPoster poster) {
        return chunk -> chunk.forEach(poster::processTransaction);
    }

    @Bean
    public Step step15(JobRepository jobRepository,
                       PlatformTransactionManager transactionManager,
                       ItemReader<DalytranRecord> dalytranReader,
                       ItemWriter<DalytranRecord> postTranWriter,
                       TransactionPoster poster,
                       StepReturnCode returnCode) {
        return new StepBuilder("STEP15", jobRepository)
                .<DalytranRecord, DalytranRecord>chunk(1, transactionManager)
                .reader(dalytranReader)
                .writer(postTranWriter)
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        poster.openFiles();
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        returnCode.set(poster.closeFiles());
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }

    @Bean
    public Job postTranJob(JobRepository jobRepository, Step step15) {
        return new JobBuilder("POSTTRAN", jobRepository)
                .start(step15)
                .build();
    }

    /**
     * Submits the job on startup. Boot's own {@code JobLauncherApplicationRunner} is not used: it
     * only auto-configures alongside a DataSource-backed job repository, and this step has no
     * database.
     */
    @Bean
    public ApplicationRunner postTranRunner(JobLauncher jobLauncher, Job postTranJob) {
        return args -> jobLauncher.run(postTranJob, new JobParameters());
    }
}
