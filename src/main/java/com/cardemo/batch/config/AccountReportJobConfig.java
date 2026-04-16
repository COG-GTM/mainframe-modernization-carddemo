package com.cardemo.batch.config;

import com.cardemo.batch.model.AccountRecord;
import com.cardemo.batch.reader.AccountFileReader;
import com.cardemo.batch.writer.AccountReportWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch job configuration for Account Data Report (CBACT01C).
 * Reads ACCTFILE sequentially and produces a CSV report with all 11 account fields.
 */
@Configuration
public class AccountReportJobConfig {

    private static final Logger log = LoggerFactory.getLogger(AccountReportJobConfig.class);

    @Value("${batch.reports.input-dir:./data/input}")
    private String inputDir;

    @Value("${batch.reports.output-dir:./data/output}")
    private String outputDir;

    @Bean
    public FlatFileItemReader<AccountRecord> accountItemReader() {
        return AccountFileReader.create(
                new FileSystemResource(inputDir + "/acctfile.csv"));
    }

    @Bean
    public FlatFileItemWriter<AccountRecord> accountItemWriter() {
        return new AccountReportWriter(
                new FileSystemResource(outputDir + "/account-report.csv"));
    }

    @Bean
    public Step accountReportStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  FlatFileItemReader<AccountRecord> accountItemReader,
                                  FlatFileItemWriter<AccountRecord> accountItemWriter) {
        return new StepBuilder("accountReportStep", jobRepository)
                .<AccountRecord, AccountRecord>chunk(10, transactionManager)
                .reader(accountItemReader)
                .writer(accountItemWriter)
                .faultTolerant()
                .noSkip(Exception.class)
                .build();
    }

    @Bean
    public Job accountReportJob(JobRepository jobRepository, Step accountReportStep) {
        log.info("Configuring Account Report Job (CBACT01C)");
        return new JobBuilder("accountReportJob", jobRepository)
                .start(accountReportStep)
                .build();
    }
}
