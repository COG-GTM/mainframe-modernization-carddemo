package com.carddemo.batch.processor;

import com.carddemo.transaction.entity.AccountEntity;
import com.carddemo.transaction.entity.CategoryBalanceEntity;
import com.carddemo.transaction.entity.DisclosureGroupEntity;
import com.carddemo.transaction.entity.TransactionEntity;
import com.carddemo.transaction.repository.AccountRepository;
import com.carddemo.transaction.repository.CategoryBalanceRepository;
import com.carddemo.transaction.repository.DisclosureGroupRepository;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Batch job replacing CBACT04C.cbl — Interest Calculation.
 *
 * COBOL Traceability: Replaces CBACT04C.cbl (INTCALC JCL job).
 * The COBOL program:
 * 1. Reads ACCOUNT file sequentially
 * 2. For each account, reads XREF to get card numbers
 * 3. Reads TCATBALF for category balances
 * 4. Reads DISCGRP for interest rates by account group + category
 * 5. Calculates interest: balance * (rate / 1200) for monthly
 * 6. Creates interest transaction in TRANSACT
 * 7. Updates ACCOUNT balance and TCATBALF
 */
@Configuration
public class InterestCalculationJob {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationJob.class);
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("1200");

    private final AccountRepository accountRepository;
    private final CategoryBalanceRepository categoryBalanceRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final TransactionRepository transactionRepository;

    public InterestCalculationJob(AccountRepository accountRepository,
                                   CategoryBalanceRepository categoryBalanceRepository,
                                   DisclosureGroupRepository disclosureGroupRepository,
                                   TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.categoryBalanceRepository = categoryBalanceRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.transactionRepository = transactionRepository;
    }

    @Bean
    public Job interestCalcJob(JobRepository jobRepository,
                                Step calculateInterestStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(calculateInterestStep)
                .build();
    }

    /**
     * Step that calculates and posts interest for all accounts.
     *
     * COBOL Traceability: Replaces CBACT04C main processing loop.
     * For each account, looks up category balances and disclosure group
     * interest rates, calculates monthly interest, and posts interest
     * transactions.
     */
    @Bean
    public Step calculateInterestStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            log.info("Starting interest calculation...");

            List<AccountEntity> accounts = accountRepository.findAll();
            int interestCount = 0;

            for (AccountEntity account : accounts) {
                if (!"Y".equals(account.getActiveStatus())) {
                    continue;
                }

                List<CategoryBalanceEntity> balances =
                        categoryBalanceRepository.findByAccountId(account.getAccountId());

                for (CategoryBalanceEntity catBal : balances) {
                    if (catBal.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }

                    // Look up interest rate from disclosure group
                    String groupId = account.getGroupId() != null
                            ? account.getGroupId() : "DEFAULT";
                    DisclosureGroupEntity discGroup = disclosureGroupRepository
                            .findByAccountGroupIdAndTransactionTypeCodeAndTransactionCategoryCode(
                                    groupId, catBal.getTypeCode(), catBal.getCategoryCode())
                            .orElse(null);

                    if (discGroup == null || discGroup.getInterestRate()
                            .compareTo(BigDecimal.ZERO) == 0) {
                        continue;
                    }

                    // Calculate monthly interest: balance * (rate / 1200)
                    BigDecimal monthlyRate = discGroup.getInterestRate()
                            .divide(MONTHS_IN_YEAR, 10, RoundingMode.HALF_UP);
                    BigDecimal interest = catBal.getBalance()
                            .multiply(monthlyRate)
                            .setScale(2, RoundingMode.HALF_UP);

                    if (interest.compareTo(BigDecimal.ZERO) > 0) {
                        // Generate transaction ID atomically from DB sequence
                        long nextNum = transactionRepository
                                .nextTransactionIdFromSequence();

                        // Create interest transaction
                        LocalDateTime now = LocalDateTime.now();
                        TransactionEntity txn = new TransactionEntity();
                        txn.setTransactionId(String.format("%016d", nextNum));
                        txn.setTypeCode("06");
                        txn.setCategoryCode(catBal.getCategoryCode());
                        txn.setSource("SYSTEM");
                        txn.setDescription("INTEREST CHARGE");
                        txn.setAmount(interest);
                        txn.setCardNumber("0000000000000000");
                        txn.setMerchantId(0L);
                        txn.setMerchantName("INTEREST");
                        txn.setMerchantCity("N/A");
                        txn.setMerchantZip("N/A");
                        txn.setOriginTimestamp(now);
                        txn.setProcessedTimestamp(now);
                        transactionRepository.save(txn);

                        // Update category balance
                        catBal.setBalance(catBal.getBalance().add(interest));
                        categoryBalanceRepository.save(catBal);

                        // Update account balance
                        account.setCurrentBalance(
                                account.getCurrentBalance().add(interest));
                        accountRepository.save(account);

                        interestCount++;
                    }
                }
            }

            log.info("Interest calculation complete. Interest transactions created: {}",
                    interestCount);
            return RepeatStatus.FINISHED;
        };

        return new StepBuilder("calculateInterestStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
