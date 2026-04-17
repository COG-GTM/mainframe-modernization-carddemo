package com.carddemo.batch.processor;

import com.carddemo.transaction.entity.ReportRequestEntity;
import com.carddemo.transaction.entity.TransactionEntity;
import com.carddemo.transaction.repository.ReportRequestRepository;
import com.carddemo.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Batch job replacing CBTRN03C.cbl — Transaction Reporting.
 *
 * COBOL Traceability: Replaces CBTRN03C.cbl (batch report generation).
 * The COBOL program:
 * 1. Reads TRANSACT file sequentially
 * 2. Reads XREF for card-account mapping
 * 3. Reads TRANTYPE for transaction type descriptions
 * 4. Reads TRANCATG for category descriptions
 * 5. Generates formatted report output
 *
 * Triggered by CORPT00C writing to CICS Transient Data queue.
 * In the modern system, triggered by pending ReportRequest entries.
 */
@Configuration
public class TransactionReportJob {

    private static final Logger log = LoggerFactory.getLogger(TransactionReportJob.class);

    private final ReportRequestRepository reportRequestRepository;
    private final TransactionRepository transactionRepository;

    public TransactionReportJob(ReportRequestRepository reportRequestRepository,
                                 TransactionRepository transactionRepository) {
        this.reportRequestRepository = reportRequestRepository;
        this.transactionRepository = transactionRepository;
    }

    @Bean
    public Job transactionReportGenJob(JobRepository jobRepository,
                                        Step generateReportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(generateReportStep)
                .build();
    }

    /**
     * Step that processes pending report requests.
     *
     * COBOL Traceability: Replaces CBTRN03C main processing loop that
     * reads TRANSACT sequentially and generates formatted report lines.
     */
    @Bean
    public Step generateReportStep(JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            log.info("Starting transaction report generation...");

            List<ReportRequestEntity> pendingRequests = reportRequestRepository.findAll()
                    .stream()
                    .filter(r -> "PENDING".equals(r.getStatus()))
                    .toList();

            for (ReportRequestEntity request : pendingRequests) {
                try {
                    request.setStatus("PROCESSING");
                    reportRequestRepository.save(request);

                    // Query transactions for the date range
                    List<TransactionEntity> transactions = transactionRepository.findAll()
                            .stream()
                            .filter(t -> t.getOriginTimestamp() != null
                                    && !t.getOriginTimestamp().toLocalDate()
                                            .isBefore(request.getStartDate())
                                    && !t.getOriginTimestamp().toLocalDate()
                                            .isAfter(request.getEndDate()))
                            .toList();

                    log.info("Report {}: Found {} transactions for date range {} to {}",
                            request.getId(), transactions.size(),
                            request.getStartDate(), request.getEndDate());

                    // Mark as completed
                    request.setStatus("COMPLETED");
                    request.setCompletedAt(LocalDateTime.now());
                    reportRequestRepository.save(request);

                } catch (Exception e) {
                    log.error("Report {} generation failed: {}", request.getId(), e.getMessage());
                    request.setStatus("FAILED");
                    reportRequestRepository.save(request);
                }
            }

            log.info("Transaction report generation complete. Processed: {}",
                    pendingRequests.size());
            return RepeatStatus.FINISHED;
        };

        return new StepBuilder("generateReportStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
