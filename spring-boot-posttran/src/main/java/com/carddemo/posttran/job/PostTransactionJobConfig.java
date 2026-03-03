package com.carddemo.posttran.job;

import com.carddemo.posttran.model.DailyTransaction;
import com.carddemo.posttran.model.ProcessedTransaction;
import com.carddemo.posttran.model.Transaction;
import com.carddemo.posttran.model.TransactionReject;
import com.carddemo.posttran.processor.TransactionPostingProcessor;
import com.carddemo.posttran.processor.TransactionValidationProcessor;
import com.carddemo.posttran.repository.AccountRepository;
import com.carddemo.posttran.repository.CardXrefRepository;
import com.carddemo.posttran.repository.CategoryBalanceRepository;
import com.carddemo.posttran.repository.TransactionRejectRepository;
import com.carddemo.posttran.repository.TransactionRepository;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Spring Batch job configuration for the PostTransactionJob.
 * Replicates the batch semantics of COBOL program CBTRN02C executed via POSTTRAN JCL.
 *
 * <pre>
 * PostTransactionJob
 *   └── Step: processTransactions
 *         ├── ItemReader  → reads from daily_transactions staging table
 *         ├── ItemProcessor (validation + posting for valid records)
 *         └── ItemWriter (classifier: valid → transactions, rejected → transaction_rejects)
 * </pre>
 */
@Configuration
public class PostTransactionJobConfig {

    private static final Logger log = LoggerFactory.getLogger(PostTransactionJobConfig.class);

    @Value("${posttran.chunk-size:10}")
    private int chunkSize;

    // ─── Item Reader ────────────────────────────────────────────────────────────

    /**
     * Reads daily transaction records from the staging table.
     * Maps to COBOL section 1000-DALYTRAN-GET-NEXT (sequential read loop).
     */
    @Bean
    public JpaPagingItemReader<DailyTransaction> dailyTransactionReader(
            EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<DailyTransaction>()
                .name("dailyTransactionReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT d FROM DailyTransaction d ORDER BY d.tranId")
                .pageSize(chunkSize)
                .build();
    }

    // ─── Item Processor ─────────────────────────────────────────────────────────

    /**
     * Creates the validation processor bean.
     */
    @Bean
    public TransactionValidationProcessor transactionValidationProcessor(
            CardXrefRepository cardXrefRepository,
            AccountRepository accountRepository) {
        return new TransactionValidationProcessor(cardXrefRepository, accountRepository);
    }

    /**
     * Creates the posting processor bean.
     */
    @Bean
    public TransactionPostingProcessor transactionPostingProcessor(
            TransactionRepository transactionRepository,
            CategoryBalanceRepository categoryBalanceRepository,
            AccountRepository accountRepository) {
        return new TransactionPostingProcessor(
                transactionRepository, categoryBalanceRepository, accountRepository);
    }

    /**
     * Composite processor: validates, then posts valid transactions inline.
     * Returns ProcessedTransaction for the writer to handle (route valid vs rejected).
     */
    @Bean
    public ItemProcessor<DailyTransaction, ProcessedTransaction> compositeProcessor(
            TransactionValidationProcessor validationProcessor,
            TransactionPostingProcessor postingProcessor) {
        return dailyTransaction -> {
            ProcessedTransaction result = validationProcessor.process(dailyTransaction);
            if (result != null && result.isValid()) {
                // Post the valid transaction (2000-POST-TRANSACTION)
                postingProcessor.post(result);
            }
            return result;
        };
    }

    // ─── Item Writer ────────────────────────────────────────────────────────────

    /**
     * Classifier-based writer: routes valid and rejected transactions.
     * - Valid transactions have already been posted by the processor; no additional write needed.
     * - Rejected transactions are written to the transaction_rejects table.
     * Maps to COBOL section 2500-WRITE-REJECT-REC for rejects.
     */
    @Bean
    public ItemWriter<ProcessedTransaction> classifierItemWriter(
            TransactionRejectRepository rejectRepository,
            AtomicLong rejectCounter) {
        return items -> {
            for (ProcessedTransaction processed : items) {
                if (!processed.isValid()) {
                    // 2500-WRITE-REJECT-REC: write reject record
                    TransactionReject reject = buildRejectRecord(processed);
                    rejectRepository.save(reject);
                    rejectCounter.incrementAndGet();
                }
            }
        };
    }

    /**
     * Builds a TransactionReject entity from a failed ProcessedTransaction.
     * Maps to COBOL: MOVE DALYTRAN-RECORD TO REJECT-TRAN-DATA /
     *                MOVE WS-VALIDATION-TRAILER TO VALIDATION-TRAILER
     */
    private TransactionReject buildRejectRecord(ProcessedTransaction processed) {
        DailyTransaction dt = processed.getDailyTransaction();
        TransactionReject reject = new TransactionReject();
        reject.setTranId(dt.getTranId());
        reject.setTranTypeCd(dt.getTranTypeCd());
        reject.setTranCatCd(dt.getTranCatCd());
        reject.setTranSource(dt.getTranSource());
        reject.setTranDesc(dt.getTranDesc());
        reject.setTranAmt(dt.getTranAmt());
        reject.setTranMerchantId(dt.getTranMerchantId());
        reject.setTranMerchantName(dt.getTranMerchantName());
        reject.setTranMerchantCity(dt.getTranMerchantCity());
        reject.setTranMerchantZip(dt.getTranMerchantZip());
        reject.setTranCardNum(dt.getTranCardNum());
        reject.setTranOrigTs(dt.getTranOrigTs());
        reject.setTranProcTs(dt.getTranProcTs());
        reject.setValidationFailReason(processed.getValidationFailReason());
        reject.setValidationFailReasonDesc(processed.getValidationFailReasonDesc());
        return reject;
    }

    // ─── Counters ───────────────────────────────────────────────────────────────

    /**
     * Thread-safe reject counter matching COBOL WS-REJECT-COUNT.
     */
    @Bean
    public AtomicLong rejectCounter() {
        return new AtomicLong(0);
    }

    // ─── Step ───────────────────────────────────────────────────────────────────

    /**
     * The processTransactions step with chunk-oriented processing.
     * Chunk-level transactions ensure that a failure in posting rolls back the
     * current chunk (equivalent to COBOL 9999-ABEND-PROGRAM for I/O errors).
     */
    @Bean
    public Step processTransactionsStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JpaPagingItemReader<DailyTransaction> dailyTransactionReader,
            ItemProcessor<DailyTransaction, ProcessedTransaction> compositeProcessor,
            ItemWriter<ProcessedTransaction> classifierItemWriter,
            AtomicLong rejectCounter) {
        return new StepBuilder("processTransactions", jobRepository)
                .<DailyTransaction, ProcessedTransaction>chunk(chunkSize, transactionManager)
                .reader(dailyTransactionReader)
                .processor(compositeProcessor)
                .writer(classifierItemWriter)
                .faultTolerant()
                .skipLimit(Integer.MAX_VALUE)
                .skip(Exception.class)
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        log.info("START OF EXECUTION OF PROGRAM CBTRN02C (Spring Batch)");
                        rejectCounter.set(0);
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        long totalRead = stepExecution.getReadCount();
                        long rejects = rejectCounter.get();
                        log.info("TRANSACTIONS PROCESSED :{}", totalRead);
                        log.info("TRANSACTIONS REJECTED  :{}", rejects);

                        // COBOL: IF WS-REJECT-COUNT > 0 MOVE 4 TO RETURN-CODE
                        if (rejects > 0) {
                            log.info("Setting exit status to COMPLETED_WITH_REJECTS (RC=4)");
                            return new ExitStatus("COMPLETED_WITH_REJECTS");
                        }
                        log.info("END OF EXECUTION OF PROGRAM CBTRN02C (Spring Batch)");
                        return ExitStatus.COMPLETED;
                    }
                })
                .build();
    }

    // ─── Job ────────────────────────────────────────────────────────────────────

    /**
     * The PostTransactionJob — equivalent to POSTTRAN JCL job running CBTRN02C.
     */
    @Bean
    public Job postTransactionJob(JobRepository jobRepository,
                                  Step processTransactionsStep) {
        return new JobBuilder("postTransactionJob", jobRepository)
                .start(processTransactionsStep)
                .build();
    }
}
