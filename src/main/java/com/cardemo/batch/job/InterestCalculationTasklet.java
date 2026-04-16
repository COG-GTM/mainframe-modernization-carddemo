package com.cardemo.batch.job;

import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionRecord;
import com.cardemo.batch.repository.TransactionCategoryBalanceRepository;
import com.cardemo.batch.service.InterestCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Spring Batch Tasklet that executes the interest calculation.
 * Reads all category balance records sequentially (matching COBOL's sequential file read),
 * processes them through the InterestCalculationService, and generates interest transactions.
 *
 * Corresponds to the main PROCEDURE DIVISION loop in CBACT04C.cbl.
 */
@Component
public class InterestCalculationTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationTasklet.class);

    private final TransactionCategoryBalanceRepository categoryBalanceRepository;
    private final InterestCalculationService interestCalculationService;

    @Value("${interest.calculation.run-date:#{null}}")
    private String configuredRunDate;

    public InterestCalculationTasklet(
            TransactionCategoryBalanceRepository categoryBalanceRepository,
            InterestCalculationService interestCalculationService) {
        this.categoryBalanceRepository = categoryBalanceRepository;
        this.interestCalculationService = interestCalculationService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("START OF EXECUTION OF INTEREST CALCULATION (CBACT04C)");

        // Resolve PARM-DATE: from job parameter, config, or current date
        String parmDate = resolveRunDate(chunkContext);
        log.info("Using run date (PARM-DATE): {}", parmDate);

        // Reset counters for this run
        interestCalculationService.resetCounters();

        // Read all category balance records in sequential order (0000-TCATBALF-OPEN + loop)
        List<TransactionCategoryBalance> records = categoryBalanceRepository.findAllOrderedByKey();
        log.info("Read {} transaction category balance records from TCATBALF", records.size());

        // Process all records
        List<TransactionRecord> generated = interestCalculationService.processAllRecords(records, parmDate);

        // Report metrics
        contribution.incrementReadCount();
        contribution.incrementWriteCount(generated.size());

        log.info("END OF EXECUTION OF INTEREST CALCULATION (CBACT04C). " +
                        "Records processed: {}, Transactions generated: {}, TranID suffix: {}",
                interestCalculationService.getRecordCount(),
                generated.size(),
                interestCalculationService.getTranIdSuffix());

        return RepeatStatus.FINISHED;
    }

    private String resolveRunDate(ChunkContext chunkContext) {
        // First try job parameter
        Object jobParmDate = chunkContext.getStepContext()
                .getJobParameters().get("runDate");
        if (jobParmDate != null && !jobParmDate.toString().isBlank()) {
            return jobParmDate.toString();
        }

        // Then try configured property
        if (configuredRunDate != null && !configuredRunDate.isBlank()) {
            return configuredRunDate;
        }

        // Default to current date
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
