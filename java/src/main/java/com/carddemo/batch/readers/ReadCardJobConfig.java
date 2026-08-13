package com.carddemo.batch.readers;

import com.carddemo.model.entity.Card;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * COBOL program: CBACT02C — read and print the card master file, driven by
 * {@code app/jcl/READCARD.jcl} (STEP05, CARDFILE = CARDDATA VSAM KSDS).
 * Copybook CVACT02Y; records are read in CARD-NUM order.
 */
@Configuration
public class ReadCardJobConfig {

    public static final String JOB_NAME = "readCardJob";
    static final String PROGRAM = "CBACT02C";

    @Bean
    JpaPagingItemReader<Card> cardMasterReader(EntityManagerFactory entityManagerFactory) {
        return VsamReaderJobSupport.reader("cardMasterReader", entityManagerFactory,
                "select c from Card c order by c.cardNumber");
    }

    @Bean
    ItemWriter<Card> cardRecordWriter() {
        return VsamReaderJobSupport.displayWriter(PROGRAM, Cbact02cDisplay::displayLines);
    }

    @Bean
    Step readCardStep(JobRepository jobRepository,
                      PlatformTransactionManager transactionManager,
                      JpaPagingItemReader<Card> cardMasterReader,
                      ItemWriter<Card> cardRecordWriter) {
        return new StepBuilder("readCardStep", jobRepository)
                .<Card, Card>chunk(100, transactionManager)
                .reader(cardMasterReader)
                .writer(cardRecordWriter)
                .build();
    }

    @Bean
    Job readCardJob(JobRepository jobRepository, Step readCardStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(VsamReaderJobSupport.executionListener(PROGRAM, "ERROR READING CARDFILE"))
                .start(readCardStep)
                .build();
    }
}
