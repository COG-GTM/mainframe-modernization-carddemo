package com.cardemo.batch.integration;

import com.cardemo.batch.config.AccountReportJobConfig;
import com.cardemo.batch.config.CardReportJobConfig;
import com.cardemo.batch.config.CardXrefReportJobConfig;
import com.cardemo.batch.config.CustomerReportJobConfig;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        AccountReportJobConfig.class,
        CardReportJobConfig.class,
        CardXrefReportJobConfig.class,
        CustomerReportJobConfig.class
})
@EnableAutoConfiguration
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:allreportstest;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.batch.jdbc.initialize-schema=always",
        "spring.batch.job.enabled=false",
        "batch.reports.input-dir=src/test/resources/data/input",
        "batch.reports.output-dir=build/test-output/all"
})
class AllReportsIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("accountReportJob")
    private Job accountReportJob;

    @Autowired
    @Qualifier("cardReportJob")
    private Job cardReportJob;

    @Autowired
    @Qualifier("cardXrefReportJob")
    private Job cardXrefReportJob;

    @Autowired
    @Qualifier("customerReportJob")
    private Job customerReportJob;

    @Test
    void shouldLoadAllFourJobConfigurations() {
        assertNotNull(accountReportJob);
        assertNotNull(cardReportJob);
        assertNotNull(cardXrefReportJob);
        assertNotNull(customerReportJob);

        assertEquals("accountReportJob", accountReportJob.getName());
        assertEquals("cardReportJob", cardReportJob.getName());
        assertEquals("cardXrefReportJob", cardXrefReportJob.getName());
        assertEquals("customerReportJob", customerReportJob.getName());
    }
}
