package com.carddemo.batch.posting;

import com.carddemo.batch.posting.DailyTransactionVerificationService.VerificationSummary;
import com.carddemo.model.entity.DailyTransaction;
import com.carddemo.repository.DailyTransactionRepository;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * COBOL programs: CBTRN01C (daily transaction verification pass) and CBTRN02C (daily transaction
 * posting), replacing JCL POSTTRAN and the DALYREJS GDG definition in DALYREJS.jcl.
 *
 * <p>The business rules live in {@link DailyTransactionVerificationService} and
 * {@link TransactionPostingService}; this class only wires them into Spring Batch. The DALYTRAN
 * sequential read becomes a repository reader ordered by DALYTRAN-ID and the DALYREJS output
 * becomes a flat file of 430-byte records ({@code carddemo.batch.posting.reject-file}).
 */
@Configuration
public class DailyPostingJobConfig {

    private static final Logger log = LoggerFactory.getLogger(DailyPostingJobConfig.class);

    /** CBTRN01C. */
    @Bean
    public Job dailyTransactionVerificationJob(JobRepository jobRepository,
                                               Step dailyTransactionVerificationStep) {
        return new JobBuilder("dailyTransactionVerificationJob", jobRepository)
                .start(dailyTransactionVerificationStep)
                .build();
    }

    @Bean
    public Step dailyTransactionVerificationStep(JobRepository jobRepository,
                                                 PlatformTransactionManager transactionManager,
                                                 DailyTransactionVerificationService verificationService) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            VerificationSummary summary = verificationService.verifyAll();
            log.info("CBTRN01C: {} transactions read, {} cards not verified, {} accounts not found",
                    summary.transactionsRead(), summary.cardsNotVerified(), summary.accountsNotFound());
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("dailyTransactionVerificationStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    /** CBTRN02C / POSTTRAN.jcl. */
    @Bean
    public Job postTransactionJob(JobRepository jobRepository, Step postTransactionStep) {
        return new JobBuilder("postTransactionJob", jobRepository)
                .start(postTransactionStep)
                .build();
    }

    @Bean
    public Step postTransactionStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager,
                                    RepositoryItemReader<DailyTransaction> dailyTransactionReader,
                                    ItemProcessor<DailyTransaction, TransactionRejectRecord> postTransactionProcessor,
                                    FlatFileItemWriter<TransactionRejectRecord> dailyRejectsWriter) {
        return new StepBuilder("postTransactionStep", jobRepository)
                .<DailyTransaction, TransactionRejectRecord>chunk(100, transactionManager)
                .reader(dailyTransactionReader)
                .processor(postTransactionProcessor)
                .writer(dailyRejectsWriter)
                .build();
    }

    /** Sequential read of DALYTRAN (0000-DALYTRAN-OPEN / 1000-DALYTRAN-GET-NEXT). */
    @Bean
    public RepositoryItemReader<DailyTransaction> dailyTransactionReader(
            DailyTransactionRepository dailyTransactionRepository) {
        return new RepositoryItemReaderBuilder<DailyTransaction>()
                .name("dailyTransactionReader")
                .repository(dailyTransactionRepository)
                .methodName("findAll")
                .sorts(Map.of("transactionId", Sort.Direction.ASC))
                .pageSize(100)
                .build();
    }

    /**
     * 1500-VALIDATE-TRAN plus 2000-POST-TRANSACTION: posted transactions are filtered out of the
     * chunk, rejected ones flow into the DALYREJS writer (2500-WRITE-REJECT-REC).
     */
    @Bean
    public ItemProcessor<DailyTransaction, TransactionRejectRecord> postTransactionProcessor(
            TransactionPostingService postingService) {
        return dailyTransaction -> postingService.post(dailyTransaction).getRejectRecord().orElse(null);
    }

    /** DALYREJS: RECFM=F, LRECL=430 (POSTTRAN.jcl). */
    @Bean
    public FlatFileItemWriter<TransactionRejectRecord> dailyRejectsWriter(
            @Value("${carddemo.batch.posting.reject-file:target/dalyrejs.txt}") String rejectFile) {
        return new FlatFileItemWriterBuilder<TransactionRejectRecord>()
                .name("dailyRejectsWriter")
                .resource(new FileSystemResource(rejectFile))
                .lineAggregator(TransactionRejectRecord::toRecordLine)
                .build();
    }
}
