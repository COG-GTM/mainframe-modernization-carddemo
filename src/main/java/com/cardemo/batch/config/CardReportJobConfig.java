package com.cardemo.batch.config;

import com.cardemo.batch.model.CardRecord;
import com.cardemo.batch.reader.CardFileReader;
import com.cardemo.batch.writer.CardReportWriter;
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
 * Spring Batch job configuration for Card Data Report (CBACT02C).
 * Reads CARDFILE sequentially and produces a CSV report.
 */
@Configuration
public class CardReportJobConfig {

    private static final Logger log = LoggerFactory.getLogger(CardReportJobConfig.class);

    @Value("${batch.reports.input-dir:./data/input}")
    private String inputDir;

    @Value("${batch.reports.output-dir:./data/output}")
    private String outputDir;

    @Bean
    public FlatFileItemReader<CardRecord> cardItemReader() {
        return CardFileReader.create(
                new FileSystemResource(inputDir + "/cardfile.csv"));
    }

    @Bean
    public FlatFileItemWriter<CardRecord> cardItemWriter() {
        return new CardReportWriter(
                new FileSystemResource(outputDir + "/card-report.csv"));
    }

    @Bean
    public Step cardReportStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               FlatFileItemReader<CardRecord> cardItemReader,
                               FlatFileItemWriter<CardRecord> cardItemWriter) {
        return new StepBuilder("cardReportStep", jobRepository)
                .<CardRecord, CardRecord>chunk(10, transactionManager)
                .reader(cardItemReader)
                .writer(cardItemWriter)
                .faultTolerant()
                .noSkip(Exception.class)
                .build();
    }

    @Bean
    public Job cardReportJob(JobRepository jobRepository, Step cardReportStep) {
        log.info("Configuring Card Report Job (CBACT02C)");
        return new JobBuilder("cardReportJob", jobRepository)
                .start(cardReportStep)
                .build();
    }
}
