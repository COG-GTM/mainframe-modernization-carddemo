package com.carddemo.batch.account;

import com.carddemo.domain.Account;
import com.carddemo.domain.Card;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Customer;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch wiring for the four read/print batch programs of CardDemo WAVE 3 (CS-10):
 *
 * <ul>
 *   <li>{@code accountFileJob} ← {@code CBACT01C} (account master)</li>
 *   <li>{@code cardFileJob} ← {@code CBACT02C} (card file)</li>
 *   <li>{@code xrefFileJob} ← {@code CBACT03C} (card cross-reference)</li>
 *   <li>{@code customerFileJob} ← {@code CBCUS01C} (customer file)</li>
 * </ul>
 *
 * <p>Each COBOL program opens its VSAM KSDS, walks it in ascending key order and {@code DISPLAY}s
 * every record. The Java equivalent is a chunk step: {@link RepositoryItemReader} (JPA
 * repository, sorted by the primary key) → {@link RecordFormatter} (record image) →
 * {@link FlatFileItemWriter} (one line per record). The output file path is the {@code outputFile}
 * job parameter, defaulting to {@code target/cbact-output/<file>.txt}.</p>
 */
@Configuration
public class PrintFileJobConfig {

    static final int CHUNK = 100;
    private static final Logger log = LoggerFactory.getLogger(PrintFileJobConfig.class);

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;

    public PrintFileJobConfig(JobRepository jobRepository, PlatformTransactionManager txManager) {
        this.jobRepository = jobRepository;
        this.txManager = txManager;
    }

    // --- CBACT01C: account master --------------------------------------------------------------

    @Bean
    public Job accountFileJob(Step accountPrintStep) {
        return new JobBuilder("accountFileJob", jobRepository).start(accountPrintStep).build();
    }

    @Bean
    public Step accountPrintStep(AccountRepository repository,
                                 FlatFileItemWriter<String> accountPrintWriter) {
        RepositoryItemReader<Account> reader = reader("accountPrintReader", repository, "acctId");
        return printStep("accountPrintStep", "CBACT01C", reader, a -> RecordFormatter.account(a), accountPrintWriter);
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<String> accountPrintWriter(
            @Value("#{jobParameters['outputFile'] ?: 'target/cbact-output/acctfile.txt'}") String outputFile) {
        return writer("accountPrintWriter", outputFile);
    }

    // --- CBACT02C: card file -------------------------------------------------------------------

    @Bean
    public Job cardFileJob(Step cardPrintStep) {
        return new JobBuilder("cardFileJob", jobRepository).start(cardPrintStep).build();
    }

    @Bean
    public Step cardPrintStep(CardRepository repository,
                              FlatFileItemWriter<String> cardPrintWriter) {
        RepositoryItemReader<Card> reader = reader("cardPrintReader", repository, "cardNum");
        return printStep("cardPrintStep", "CBACT02C", reader, c -> RecordFormatter.card(c), cardPrintWriter);
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<String> cardPrintWriter(
            @Value("#{jobParameters['outputFile'] ?: 'target/cbact-output/cardfile.txt'}") String outputFile) {
        return writer("cardPrintWriter", outputFile);
    }

    // --- CBACT03C: card cross-reference --------------------------------------------------------

    @Bean
    public Job xrefFileJob(Step xrefPrintStep) {
        return new JobBuilder("xrefFileJob", jobRepository).start(xrefPrintStep).build();
    }

    @Bean
    public Step xrefPrintStep(CardXrefRepository repository,
                              FlatFileItemWriter<String> xrefPrintWriter) {
        RepositoryItemReader<CardXref> reader = reader("xrefPrintReader", repository, "xrefCardNum");
        return printStep("xrefPrintStep", "CBACT03C", reader, x -> RecordFormatter.cardXref(x), xrefPrintWriter);
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<String> xrefPrintWriter(
            @Value("#{jobParameters['outputFile'] ?: 'target/cbact-output/xreffile.txt'}") String outputFile) {
        return writer("xrefPrintWriter", outputFile);
    }

    // --- CBCUS01C: customer file ---------------------------------------------------------------

    @Bean
    public Job customerFileJob(Step customerPrintStep) {
        return new JobBuilder("customerFileJob", jobRepository).start(customerPrintStep).build();
    }

    @Bean
    public Step customerPrintStep(CustomerRepository repository,
                                  FlatFileItemWriter<String> customerPrintWriter) {
        RepositoryItemReader<Customer> reader = reader("customerPrintReader", repository, "custId");
        return printStep("customerPrintStep", "CBCUS01C", reader, c -> RecordFormatter.customer(c), customerPrintWriter);
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<String> customerPrintWriter(
            @Value("#{jobParameters['outputFile'] ?: 'target/cbact-output/custfile.txt'}") String outputFile) {
        return writer("customerPrintWriter", outputFile);
    }

    // --- shared builders -----------------------------------------------------------------------

    private <T> Step printStep(String name, String program, RepositoryItemReader<T> reader,
                               ItemProcessor<T, String> processor, FlatFileItemWriter<String> writer) {
        return new StepBuilder(name, jobRepository)
                .<T, String>chunk(CHUNK, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new ProgramLoggingListener(program))
                .build();
    }

    private <T, R extends PagingAndSortingRepository<T, ?>> RepositoryItemReader<T> reader(
            String name, R repository, String sortProperty) {
        Map<String, Sort.Direction> sorts = Collections.singletonMap(sortProperty, Sort.Direction.ASC);
        return new RepositoryItemReaderBuilder<T>()
                .name(name)
                .repository(repository)
                .methodName("findAll")
                .sorts(sorts)
                .pageSize(CHUNK)
                .build();
    }

    private FlatFileItemWriter<String> writer(String name, String outputFile) {
        return new FlatFileItemWriterBuilder<String>()
                .name(name)
                .resource(new FileSystemResource(outputFile))
                .lineAggregator(new PassThroughLineAggregator<>())
                .shouldDeleteIfExists(true)
                .build();
    }

    /** Logs the {@code START OF EXECUTION} / {@code END OF EXECUTION} banners of the COBOL programs. */
    private static final class ProgramLoggingListener
            implements org.springframework.batch.core.StepExecutionListener {
        private final String program;

        ProgramLoggingListener(String program) {
            this.program = program;
        }

        @Override
        public void beforeStep(org.springframework.batch.core.StepExecution stepExecution) {
            log.info("START OF EXECUTION OF PROGRAM {}", program);
        }

        @Override
        public org.springframework.batch.core.ExitStatus afterStep(
                org.springframework.batch.core.StepExecution stepExecution) {
            log.info("END OF EXECUTION OF PROGRAM {} ({} records)", program, stepExecution.getWriteCount());
            return stepExecution.getExitStatus();
        }
    }
}
