package com.carddemo.batch.readers;

import com.carddemo.model.entity.Customer;
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
 * COBOL program: CBCUS01C — read and print the customer master file, driven by
 * {@code app/jcl/READCUST.jcl} (STEP05, CUSTFILE = CUSTDATA VSAM KSDS).
 * Copybook CVCUS01Y; records are read in CUST-ID order.
 */
@Configuration
public class ReadCustomerJobConfig {

    public static final String JOB_NAME = "readCustomerJob";
    static final String PROGRAM = "CBCUS01C";

    @Bean
    JpaPagingItemReader<Customer> customerMasterReader(EntityManagerFactory entityManagerFactory) {
        return VsamReaderJobSupport.reader("customerMasterReader", entityManagerFactory,
                "select c from Customer c order by c.customerId");
    }

    @Bean
    ItemWriter<Customer> customerRecordWriter() {
        return VsamReaderJobSupport.displayWriter(PROGRAM, Cbcus01cDisplay::displayLines);
    }

    @Bean
    Step readCustomerStep(JobRepository jobRepository,
                          PlatformTransactionManager transactionManager,
                          JpaPagingItemReader<Customer> customerMasterReader,
                          ItemWriter<Customer> customerRecordWriter) {
        return new StepBuilder("readCustomerStep", jobRepository)
                .<Customer, Customer>chunk(100, transactionManager)
                .reader(customerMasterReader)
                .writer(customerRecordWriter)
                .build();
    }

    @Bean
    Job readCustomerJob(JobRepository jobRepository, Step readCustomerStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(VsamReaderJobSupport.executionListener(PROGRAM, "ERROR READING CUSTOMER FILE"))
                .start(readCustomerStep)
                .build();
    }
}
