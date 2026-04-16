package com.cardemo.batch.config;

import com.cardemo.batch.model.CustomerRecord;
import com.cardemo.batch.reader.CustomerFileReader;
import com.cardemo.batch.writer.CustomerReportWriter;
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
 * Spring Batch job configuration for Customer Data Report (CBCUS01C).
 * Reads CUSTFILE sequentially and produces a CSV report.
 */
@Configuration
public class CustomerReportJobConfig {

    private static final Logger log = LoggerFactory.getLogger(CustomerReportJobConfig.class);

    @Value("${batch.reports.input-dir:./data/input}")
    private String inputDir;

    @Value("${batch.reports.output-dir:./data/output}")
    private String outputDir;

    @Bean
    public FlatFileItemReader<CustomerRecord> customerItemReader() {
        return CustomerFileReader.create(
                new FileSystemResource(inputDir + "/custfile.csv"));
    }

    @Bean
    public FlatFileItemWriter<CustomerRecord> customerItemWriter() {
        return new CustomerReportWriter(
                new FileSystemResource(outputDir + "/customer-report.csv"));
    }

    @Bean
    public Step customerReportStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   FlatFileItemReader<CustomerRecord> customerItemReader,
                                   FlatFileItemWriter<CustomerRecord> customerItemWriter) {
        return new StepBuilder("customerReportStep", jobRepository)
                .<CustomerRecord, CustomerRecord>chunk(10, transactionManager)
                .reader(customerItemReader)
                .writer(customerItemWriter)
                .faultTolerant()
                .noSkip(Exception.class)
                .build();
    }

    @Bean
    public Job customerReportJob(JobRepository jobRepository, Step customerReportStep) {
        log.info("Configuring Customer Report Job (CBCUS01C)");
        return new JobBuilder("customerReportJob", jobRepository)
                .start(customerReportStep)
                .build();
    }
}
