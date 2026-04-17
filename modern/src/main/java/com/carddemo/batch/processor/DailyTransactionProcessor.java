package com.carddemo.batch.processor;

import com.carddemo.transaction.entity.AccountEntity;
import com.carddemo.transaction.entity.CategoryBalanceEntity;
import com.carddemo.transaction.entity.TransactionEntity;
import com.carddemo.transaction.repository.AccountRepository;
import com.carddemo.transaction.repository.CardXrefRepository;
import com.carddemo.transaction.repository.CategoryBalanceRepository;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Batch job replacing CBTRN01C + CBTRN02C pipeline.
 *
 * COBOL Traceability:
 * - CBTRN01C.cbl: Daily Transaction Validation
 *   Reads DALYTRAN (daily input), validates against CUSTOMER, CARD, ACCOUNT,
 *   XREF, TRANSACT files. Enriches transactions with merchant data.
 *
 * - CBTRN02C.cbl: Transaction Posting
 *   Posts validated daily transactions to TRANSACT master file.
 *   Updates ACCOUNT balances and TCATBALF category balances.
 *   Writes rejected transactions to DALYREJS.
 *
 * In the COBOL system, these run as JCL job steps (POSTTRAN) with
 * sequential file I/O and checkpoint/restart capability.
 */
@Configuration
public class DailyTransactionProcessor {

    private static final Logger log = LoggerFactory.getLogger(DailyTransactionProcessor.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CategoryBalanceRepository categoryBalanceRepository;

    public DailyTransactionProcessor(TransactionRepository transactionRepository,
                                      AccountRepository accountRepository,
                                      CardXrefRepository cardXrefRepository,
                                      CategoryBalanceRepository categoryBalanceRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.categoryBalanceRepository = categoryBalanceRepository;
    }

    @Bean
    public Job dailyTransactionJob(JobRepository jobRepository,
                                    Step postTransactionsStep) {
        return new JobBuilder("dailyTransactionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(postTransactionsStep)
                .build();
    }

    /**
     * Step that posts transactions and updates account balances.
     *
     * COBOL Traceability: Replaces CBTRN02C main processing loop:
     * PERFORM UNTIL END-OF-FILE
     *   READ daily transaction
     *   WRITE to TRANSACT master
     *   REWRITE ACCOUNT balance
     *   REWRITE TCATBALF category balance
     * END-PERFORM
     */
    @Bean
    public Step postTransactionsStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            log.info("Starting daily transaction posting...");

            // Only process unposted transactions (equivalent of reading DALYTRAN input)
            List<TransactionEntity> transactions = transactionRepository.findByPostedFalse();
            int processedCount = 0;

            for (TransactionEntity txn : transactions) {
                // Resolve account ID via card cross-reference
                // (replaces COBOL's card-to-account lookup via CXACAIX)
                String accountId = cardXrefRepository.findById(txn.getCardNumber())
                        .map(xref -> xref.getAccountId())
                        .orElse(null);
                if (accountId == null) {
                    log.warn("Card {} not found in cross-reference, skipping",
                            txn.getCardNumber());
                    continue;
                }

                // Update account balance (replaces REWRITE on ACCTDAT)
                accountRepository.findById(accountId)
                        .ifPresent(account -> {
                            BigDecimal newBalance = account.getCurrentBalance()
                                    .add(txn.getAmount());
                            account.setCurrentBalance(newBalance);
                            accountRepository.save(account);
                        });

                // Update category balance (replaces REWRITE on TCATBALF)
                CategoryBalanceEntity.CategoryBalanceId balanceId =
                        new CategoryBalanceEntity.CategoryBalanceId(
                                accountId, txn.getTypeCode(), txn.getCategoryCode());
                categoryBalanceRepository.findById(balanceId).ifPresent(catBal -> {
                    BigDecimal newBal = catBal.getBalance().add(txn.getAmount());
                    catBal.setBalance(newBal);
                    categoryBalanceRepository.save(catBal);
                });

                // Mark transaction as posted so it won't be reprocessed
                txn.setPosted(true);
                transactionRepository.save(txn);

                processedCount++;
            }

            log.info("Daily transaction posting complete. Processed: {}", processedCount);
            return RepeatStatus.FINISHED;
        };

        return new StepBuilder("postTransactionsStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

}
