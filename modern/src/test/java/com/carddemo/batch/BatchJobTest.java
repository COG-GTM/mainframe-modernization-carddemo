package com.carddemo.batch;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests that Spring Batch jobs are properly configured and can be loaded.
 *
 * COBOL Traceability: Verifies that batch job definitions replacing
 * CBTRN01C/CBTRN02C (daily posting), CBACT04C (interest calculation),
 * and CBTRN03C (report generation) are properly wired in Spring context.
 */
@SpringBootTest
class BatchJobTest {

    @Autowired
    @Qualifier("dailyTransactionJob")
    private Job dailyTransactionJob;

    @Autowired
    @Qualifier("interestCalcJob")
    private Job interestCalcJob;

    @Autowired
    @Qualifier("transactionReportGenJob")
    private Job transactionReportGenJob;

    @Test
    void dailyTransactionJob_shouldBeConfigured() {
        assertNotNull(dailyTransactionJob);
        assertEquals("dailyTransactionJob", dailyTransactionJob.getName());
    }

    @Test
    void interestCalculationJob_shouldBeConfigured() {
        assertNotNull(interestCalcJob);
        assertEquals("interestCalculationJob", interestCalcJob.getName());
    }

    @Test
    void transactionReportJob_shouldBeConfigured() {
        assertNotNull(transactionReportGenJob);
        assertEquals("transactionReportJob", transactionReportGenJob.getName());
    }
}
