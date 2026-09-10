package com.carddemo.batch.readers;

import com.carddemo.model.entity.CardXref;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * COBOL program: CBACT03C — read and print the card cross reference file, driven by
 * {@code app/jcl/READXREF.jcl} (STEP05, XREFFILE = CARDXREF VSAM KSDS).
 * Copybook CVACT03Y; records are read in XREF-CARD-NUM order.
 */
@Configuration
public class ReadXrefJobConfig {

    public static final String JOB_NAME = "readXrefJob";
    static final String PROGRAM = "CBACT03C";

    @Bean
    @StepScope
    JpaPagingItemReader<CardXref> cardXrefReader(EntityManagerFactory entityManagerFactory) {
        return VsamReaderJobSupport.reader("cardXrefReader", entityManagerFactory,
                "select x from CardXref x order by x.cardNumber");
    }

    @Bean
    ItemWriter<CardXref> cardXrefRecordWriter() {
        return VsamReaderJobSupport.displayWriter(PROGRAM, Cbact03cDisplay::displayLines);
    }

    @Bean
    Step readXrefStep(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      JpaPagingItemReader<CardXref> cardXrefReader,
                      ItemWriter<CardXref> cardXrefRecordWriter) {
        return new StepBuilder("readXrefStep", jobRepository)
                .<CardXref, CardXref>chunk(100, transactionManager)
                .reader(cardXrefReader)
                .writer(cardXrefRecordWriter)
                .build();
    }

    @Bean
    Job readXrefJob(JobRepository jobRepository, Step readXrefStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(VsamReaderJobSupport.executionListener(PROGRAM, "ERROR READING XREFFILE"))
                .start(readXrefStep)
                .build();
    }
}
