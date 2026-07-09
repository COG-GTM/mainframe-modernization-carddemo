package com.carddemo.batch.posting;

import com.carddemo.domain.DailyTransaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch wiring for the CardDemo POSTTRAN job — the Java replacement for the legacy
 * {@code CBTRN01C}/{@code CBTRN02C}/{@code CBTRN03C} batch programs.
 *
 * <ul>
 *   <li>{@code postTranJob} → {@code postTranStep}: a chunk step
 *       (ItemReader {@link DailyTransaction} → {@link TransactionPostingProcessor} →
 *       {@link PostingItemWriter}) that validates and posts the daily transactions
 *       ({@code CBTRN02C}; the read/validate loop of {@code CBTRN01C} is subsumed by the
 *       processor's XREF/account lookups). Chunk size is 1 so each posted transaction commits
 *       before the next is read, reproducing the record-by-record accumulation of the
 *       sequential COBOL loop.</li>
 *   <li>{@code transactionReportJob} → {@code transactionReportStep}: a tasklet that renders the
 *       daily transaction detail report ({@code CBTRN03C}) via {@link TransactionReportService}
 *       for the {@code startDate}/{@code endDate} job parameters and writes it to
 *       {@code outputFile}.</li>
 * </ul>
 *
 * <p>Both jobs are launched explicitly (the app runs with {@code spring.batch.job.enabled=false});
 * they are not auto-started on boot.</p>
 */
@Configuration
public class PostTranJobConfig {

    static final String RUN_DATE_START = "startDate";
    static final String RUN_DATE_END = "endDate";
    static final String OUTPUT_FILE = "outputFile";
    static final String CTX_GRAND_TOTAL = "report.grandTotal";
    static final String CTX_TRAN_COUNT = "report.transactionCount";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;

    public PostTranJobConfig(JobRepository jobRepository, PlatformTransactionManager txManager) {
        this.jobRepository = jobRepository;
        this.txManager = txManager;
    }

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock postingClock() {
        return Clock.systemDefaultZone();
    }

    // --- posting job -----------------------------------------------------------------------

    @Bean
    public TransactionPostingProcessor postingProcessor(CardXrefRepository cardXrefRepository,
                                                        AccountRepository accountRepository,
                                                        TransactionCategoryBalanceRepository categoryBalanceRepository,
                                                        Clock postingClock) {
        return new TransactionPostingProcessor(
                cardXrefRepository, accountRepository, categoryBalanceRepository, postingClock);
    }

    @Bean
    public PostingItemWriter postingItemWriter(TransactionRepository transactionRepository,
                                               AccountRepository accountRepository,
                                               TransactionCategoryBalanceRepository categoryBalanceRepository,
                                               JdbcTemplate postingJdbcTemplate) {
        return new PostingItemWriter(
                transactionRepository, accountRepository, categoryBalanceRepository, postingJdbcTemplate);
    }

    @Bean
    public RepositoryItemReader<DailyTransaction> postingItemReader(
            DailyTransactionRepository dailyTransactionRepository) {
        return new RepositoryItemReaderBuilder<DailyTransaction>()
                .name("postTranDailyTransactionReader")
                .repository(dailyTransactionRepository)
                .methodName("findAll")
                .pageSize(100)
                .sorts(Map.of("dalytranId", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public Step postTranStep(RepositoryItemReader<DailyTransaction> postingItemReader,
                             TransactionPostingProcessor postingProcessor,
                             PostingItemWriter postingItemWriter) {
        return new StepBuilder("postTranStep", jobRepository)
                .<DailyTransaction, PostingResult>chunk(1, txManager)
                .reader(postingItemReader)
                .processor(postingProcessor)
                .writer(postingItemWriter)
                .listener(postingItemWriter)
                .build();
    }

    @Bean
    public Job postTranJob(Step postTranStep) {
        return new JobBuilder("postTranJob", jobRepository)
                .start(postTranStep)
                .build();
    }

    // --- report job ------------------------------------------------------------------------

    @Bean
    public Step transactionReportStep(TransactionReportService reportService) {
        return new StepBuilder("transactionReportStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Map<String, Object> params = chunkContext.getStepContext().getJobParameters();
                    String startDate = String.valueOf(params.get(RUN_DATE_START));
                    String endDate = String.valueOf(params.get(RUN_DATE_END));
                    Object outputFile = params.get(OUTPUT_FILE);

                    TransactionReportService.ReportResult report = reportService.generate(startDate, endDate);

                    if (outputFile != null) {
                        writeReport(Path.of(String.valueOf(outputFile)), report.render());
                    }
                    contribution.getStepExecution().getExecutionContext()
                            .put(CTX_GRAND_TOTAL, report.getGrandTotal().toPlainString());
                    contribution.getStepExecution().getExecutionContext()
                            .putInt(CTX_TRAN_COUNT, report.getTransactionCount());
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }

    @Bean
    public Job transactionReportJob(Step transactionReportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
                .start(transactionReportStep)
                .build();
    }

    private static void writeReport(Path path, String content) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write transaction report to " + path, e);
        }
    }
}
