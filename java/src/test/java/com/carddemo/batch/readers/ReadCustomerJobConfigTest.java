package com.carddemo.batch.readers;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/** Verifies the CBCUS01C migration (JCL READCUST) against the sample CUSTDATA extract. */
class ReadCustomerJobConfigTest extends AbstractReaderJobTest {

    @Autowired
    @Qualifier(ReadCustomerJobConfig.JOB_NAME)
    private Job readCustomerJob;

    @Test
    void readsEveryCustomerRecordAndDisplaysItTwice() throws Exception {
        ListAppender<ILoggingEvent> output = captureOutput(ReadCustomerJobConfig.PROGRAM);

        JobExecution execution = run(readCustomerJob);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        StepExecution step = execution.getStepExecutions().iterator().next();
        assertThat(step.getReadCount()).isEqualTo(SAMPLE_RECORD_COUNT);

        List<String> lines = recordLines(output, ReadCustomerJobConfig.PROGRAM);
        // CBCUS01C displays the record in 1000-CUSTFILE-GET-NEXT and again in the read loop.
        assertThat(lines).hasSize(SAMPLE_RECORD_COUNT * 2);
        String first = sampleRecord("custdata.txt", 0, 500);
        assertThat(lines.get(0)).isEqualTo(first);
        assertThat(lines.get(1)).isEqualTo(first);
        assertThat(first).startsWith("000000001Immanuel");
        assertThat(first).hasSize(500);
    }
}
