package com.carddemo.batch;

import com.carddemo.domain.DailyTransaction;
import com.carddemo.loader.LegacyDataLoader;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.service.InterestCalculationService;
import com.carddemo.service.StatementService;
import com.carddemo.service.TransactionPostingService;
import com.carddemo.service.TransactionReportService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch replacements for the CardDemo JCL jobs.
 *
 * <p>Job names keep the JCL names so the mapping stays obvious: POSTTRAN (CBTRN02C), INTCALC
 * (CBACT04C), CREASTMT (CBSTM03A), TRANREPT (CBTRN03C) and the print/read jobs CBACT01C,
 * CBACT02C, CBACT03C, CBCUS01C and CBTRN01C. Loading the legacy extracts replaces the IDCAMS
 * REPRO steps.
 */
@Configuration
public class BatchConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfiguration(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job loadLegacyDataJob(LegacyDataLoader loader) {
        return job("loadLegacyDataJob", tasklet("loadLegacyDataStep", (contribution, chunkContext) -> {
            loader.loadAll();
            return RepeatStatus.FINISHED;
        }));
    }

    /** POSTTRAN - CBTRN02C. */
    @Bean
    public Job postTransactionsJob(DailyTransactionRepository dailyTransactions,
                                   TransactionPostingService posting) {
        return job("postTransactionsJob", tasklet("postTransactionsStep", (contribution, chunkContext) -> {
            int posted = 0;
            int rejected = 0;
            for (DailyTransaction daily : dailyTransactions.findAll()) {
                if (posting.post(daily).posted()) {
                    posted++;
                } else {
                    rejected++;
                }
            }
            contribution.incrementWriteCount(posted);
            log.info("POSTTRAN finished: {} posted, {} rejected", posted, rejected);
            if (rejected > 0) {
                // CBTRN02C ended with RETURN-CODE 4 when at least one transaction was rejected.
                contribution.getStepExecution().getExecutionContext().putInt("returnCode", 4);
            }
            return RepeatStatus.FINISHED;
        }));
    }

    /** INTCALC - CBACT04C. */
    @Bean
    public Job interestCalculationJob(TransactionCategoryBalanceRepository categoryBalances,
                                      InterestCalculationService interest) {
        return job("interestCalculationJob", tasklet("interestCalculationStep", (contribution, chunkContext) -> {
            Set<Long> accountIds = new LinkedHashSet<>();
            categoryBalances.findAllByOrderByIdAccountIdAscIdTypeCodeAscIdCategoryCodeAsc()
                    .forEach(balance -> accountIds.add(balance.getId().getAccountId()));
            accountIds.forEach(interest::calculateForAccount);
            contribution.incrementWriteCount(accountIds.size());
            return RepeatStatus.FINISHED;
        }));
    }

    /** CREASTMT - CBSTM03A. */
    @Bean
    public Job statementGenerationJob(StatementService statements) {
        return job("statementGenerationJob", tasklet("statementGenerationStep", (contribution, chunkContext) -> {
            log.info("CREASTMT wrote {}", statements.writeAll());
            return RepeatStatus.FINISHED;
        }));
    }

    /** TRANREPT - CBTRN03C. The report type and dates come in as job parameters. */
    @Bean
    public Job transactionReportJob(TransactionReportService reports) {
        return job("transactionReportJob", tasklet("transactionReportStep", (contribution, chunkContext) -> {
            var parameters = contribution.getStepExecution().getJobParameters();
            String reportType = parameters.getString("reportType", "MONTHLY");
            log.info("TRANREPT wrote {}", reports.generate(
                    reportType, parameters.getString("startDate"), parameters.getString("endDate")));
            return RepeatStatus.FINISHED;
        }));
    }

    /** CBACT01C - read and print the account master file. */
    @Bean
    public Job printAccountsJob(AccountRepository accounts) {
        return job("printAccountsJob", printJob("printAccountsStep", () -> accounts.findAll().stream()
                .map(account -> account.getId() + " " + account.getActiveStatus() + " " + account.getCurrentBalance())
                .toList()));
    }

    /** CBACT02C - read and print the card master file. */
    @Bean
    public Job printCardsJob(CardRepository cards) {
        return job("printCardsJob", printJob("printCardsStep", () -> cards.findAll().stream()
                .map(card -> card.getCardNumber() + " " + card.getAccountId() + " " + card.getActiveStatus())
                .toList()));
    }

    /** CBACT03C - read and print the card cross reference file. */
    @Bean
    public Job printCardXrefJob(CardXrefRepository xrefs) {
        return job("printCardXrefJob", printJob("printCardXrefStep", () -> xrefs.findAll().stream()
                .map(xref -> xref.getCardNumber() + " " + xref.getCustomerId() + " " + xref.getAccountId())
                .toList()));
    }

    /** CBCUS01C - read and print the customer master file. */
    @Bean
    public Job printCustomersJob(CustomerRepository customers) {
        return job("printCustomersJob", printJob("printCustomersStep", () -> customers.findAll().stream()
                .map(customer -> customer.getId() + " " + customer.getFirstName() + " " + customer.getLastName())
                .toList()));
    }

    /** CBTRN01C - read and print the daily transaction file. */
    @Bean
    public Job printDailyTransactionsJob(DailyTransactionRepository dailyTransactions) {
        return job("printDailyTransactionsJob", printJob("printDailyTransactionsStep",
                () -> dailyTransactions.findAll().stream()
                        .map(daily -> daily.getId() + " " + daily.getCardNumber() + " " + daily.getAmount())
                        .toList()));
    }

    private Step printJob(String stepName, java.util.function.Supplier<List<String>> lines) {
        return tasklet(stepName, (contribution, chunkContext) -> {
            List<String> rows = lines.get();
            rows.forEach(log::info);
            contribution.incrementReadCount();
            return RepeatStatus.FINISHED;
        });
    }

    private Job job(String name, Step step) {
        return new JobBuilder(name, jobRepository).start(step).build();
    }

    private Step tasklet(String name, org.springframework.batch.core.step.tasklet.Tasklet tasklet) {
        return new StepBuilder(name, jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
