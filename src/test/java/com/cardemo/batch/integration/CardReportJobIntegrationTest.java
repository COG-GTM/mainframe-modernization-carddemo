package com.cardemo.batch.integration;

import com.cardemo.batch.config.CardReportJobConfig;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBatchTest
@SpringJUnitConfig(classes = {CardReportJobConfig.class})
@EnableAutoConfiguration
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:cardjobtest;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.batch.jdbc.initialize-schema=always",
        "batch.reports.input-dir=src/test/resources/data/input",
        "batch.reports.output-dir=build/test-output/card"
})
class CardReportJobIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("cardReportJob")
    private Job cardReportJob;

    @Test
    void shouldRunCardReportJobSuccessfully() throws Exception {
        Files.createDirectories(Path.of("build/test-output/card"));

        jobLauncherTestUtils.setJob(cardReportJob);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(
                new JobParametersBuilder()
                        .addLong("run.id", System.currentTimeMillis())
                        .toJobParameters());

        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        Path outputFile = Path.of("build/test-output/card/card-report.csv");
        assertTrue(Files.exists(outputFile));

        List<String> lines = Files.readAllLines(outputFile);
        assertTrue(lines.size() >= 2);
        assertTrue(lines.get(0).contains("CARD-NUM"));
    }
}
