package com.carddemo.batch.report;

import com.carddemo.model.entity.Transaction;
import com.carddemo.repository.TransactionRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * COBOL job: {@code app/jcl/TRANREPT.jcl} STEP10R (PGM=CBTRN03C).
 *
 * <p>The SORT step that precedes CBTRN03C orders the TRANSACT extract by TRAN-CARD-NUM, which
 * is reproduced by the repository sort; the date range comes from the {@code startDate} /
 * {@code endDate} job parameters (the DATEPARM DD) and the report is written to the file named
 * by the {@code reportFile} job parameter (the TRANREPT DD, LRECL 133).
 */
@Configuration
public class TransactionDetailReportJobConfig {

    public static final String JOB_NAME = "transactionDetailReportJob";

    /** Date range coded in the SORT step of TRANREPT.jcl. */
    public static final String DEFAULT_START_DATE = "2022-01-01";
    public static final String DEFAULT_END_DATE = "2022-07-06";

    private static final Sort CARD_NUMBER_ORDER = Sort.by("cardNumber", "transactionId");

    private static final Logger log = LoggerFactory.getLogger(TransactionDetailReportJobConfig.class);

    @Bean
    public Job transactionDetailReportJob(JobRepository jobRepository, Step transactionDetailReportStep) {
        return new JobBuilder(JOB_NAME, jobRepository).start(transactionDetailReportStep).build();
    }

    @Bean
    public Step transactionDetailReportStep(JobRepository jobRepository,
                                            PlatformTransactionManager transactionManager,
                                            TransactionRepository transactionRepository,
                                            TransactionDetailReportService reportService) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            var parameters = chunkContext.getStepContext().getJobParameters();
            ReportDateRange range = new ReportDateRange(
                    (String) parameters.getOrDefault("startDate", DEFAULT_START_DATE),
                    (String) parameters.getOrDefault("endDate", DEFAULT_END_DATE));
            List<Transaction> transactions = transactionRepository.findAll(CARD_NUMBER_ORDER);
            List<String> lines = reportService.generate(transactions, range);
            String reportFile = (String) parameters.get("reportFile");
            if (reportFile != null) {
                write(Path.of(reportFile), lines);
            }
            log.info("CBTRN03C: wrote {} report lines for {} to {}", lines.size(), range, reportFile);
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("transactionDetailReportStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    private static void write(Path file, List<String> lines) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        Files.write(file, lines, StandardCharsets.ISO_8859_1);
    }
}
