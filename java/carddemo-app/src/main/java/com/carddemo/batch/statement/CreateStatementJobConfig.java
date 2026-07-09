package com.carddemo.batch.statement;

import com.carddemo.domain.Account;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TransactionRepository;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch {@code creastmtJob} — the Java replacement for the legacy {@code CREASTMT} job
 * (COBOL {@code CBSTM03A} driver + its {@code CBSTM03B} file-handling subroutine), which prints
 * an account statement per account in both plain-text and HTML.
 *
 * <p>Single chunk-oriented step: {@link RepositoryItemReader} over {@link Account}
 * (ordered by {@code acctId}) → {@link CreateStatementItemProcessor} (resolve cardholder +
 * gather transactions + total in {@link java.math.BigDecimal}) →
 * {@link StatementItemWriter} (render per {@code COSTM01} layout to the {@code STMTFILE} and
 * {@code HTMLFILE} equivalents).</p>
 *
 * <p>The output directory defaults to {@code target/statements} and can be overridden with the
 * {@code carddemo.batch.statement.output-dir} property.</p>
 */
@Configuration
public class CreateStatementJobConfig {

    static final int CHUNK = 100;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;

    public CreateStatementJobConfig(JobRepository jobRepository, PlatformTransactionManager txManager) {
        this.jobRepository = jobRepository;
        this.txManager = txManager;
    }

    @Bean
    public Job creastmtJob(Step createStatementStep) {
        return new JobBuilder("creastmtJob", jobRepository)
                .start(createStatementStep)
                .build();
    }

    @Bean
    public Step createStatementStep(RepositoryItemReader<Account> statementAccountReader,
                                    CreateStatementItemProcessor statementProcessor,
                                    StatementItemWriter statementWriter) {
        return new StepBuilder("createStatementStep", jobRepository)
                .<Account, AccountStatement>chunk(CHUNK, txManager)
                .reader(statementAccountReader)
                .processor(statementProcessor)
                .writer(statementWriter)
                .stream(statementWriter)
                .build();
    }

    @Bean
    public RepositoryItemReader<Account> statementAccountReader(AccountRepository accountRepository) {
        return new RepositoryItemReaderBuilder<Account>()
                .name("statementAccountReader")
                .repository(accountRepository)
                .methodName("findAll")
                .pageSize(CHUNK)
                .sorts(Map.of("acctId", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public CreateStatementItemProcessor statementProcessor(CardXrefRepository cardXrefRepository,
                                                           CustomerRepository customerRepository,
                                                           TransactionRepository transactionRepository) {
        return new CreateStatementItemProcessor(cardXrefRepository, customerRepository,
                transactionRepository);
    }

    @Bean
    public StatementItemWriter statementWriter(
            @Value("${carddemo.batch.statement.output-dir:target/statements}") String outputDir) {
        return new StatementItemWriter(Path.of(outputDir));
    }
}
