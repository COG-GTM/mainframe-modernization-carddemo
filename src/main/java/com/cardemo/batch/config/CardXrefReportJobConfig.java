package com.cardemo.batch.config;

import com.cardemo.batch.model.CardXrefRecord;
import com.cardemo.batch.reader.CardXrefFileReader;
import com.cardemo.batch.writer.CardXrefReportWriter;
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
 * Spring Batch job configuration for Cross-Reference Data Report (CBACT03C).
 * Reads XREFFILE sequentially and produces a CSV report.
 */
@Configuration
public class CardXrefReportJobConfig {

    private static final Logger log = LoggerFactory.getLogger(CardXrefReportJobConfig.class);

    @Value("${batch.reports.input-dir:./data/input}")
    private String inputDir;

    @Value("${batch.reports.output-dir:./data/output}")
    private String outputDir;

    @Bean
    public FlatFileItemReader<CardXrefRecord> cardXrefItemReader() {
        return CardXrefFileReader.create(
                new FileSystemResource(inputDir + "/xreffile.csv"));
    }

    @Bean
    public FlatFileItemWriter<CardXrefRecord> cardXrefItemWriter() {
        return new CardXrefReportWriter(
                new FileSystemResource(outputDir + "/xref-report.csv"));
    }

    @Bean
    public Step cardXrefReportStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   FlatFileItemReader<CardXrefRecord> cardXrefItemReader,
                                   FlatFileItemWriter<CardXrefRecord> cardXrefItemWriter) {
        return new StepBuilder("cardXrefReportStep", jobRepository)
                .<CardXrefRecord, CardXrefRecord>chunk(10, transactionManager)
                .reader(cardXrefItemReader)
                .writer(cardXrefItemWriter)
                .faultTolerant()
                .noSkip(Exception.class)
                .build();
    }

    @Bean
    public Job cardXrefReportJob(JobRepository jobRepository, Step cardXrefReportStep) {
        log.info("Configuring Cross-Reference Report Job (CBACT03C)");
        return new JobBuilder("cardXrefReportJob", jobRepository)
                .start(cardXrefReportStep)
                .build();
    }
}
