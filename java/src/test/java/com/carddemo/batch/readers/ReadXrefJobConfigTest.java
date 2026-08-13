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

/** Verifies the CBACT03C migration (JCL READXREF) against the sample CARDXREF extract. */
class ReadXrefJobConfigTest extends AbstractReaderJobTest {

    @Autowired
    @Qualifier(ReadXrefJobConfig.JOB_NAME)
    private Job readXrefJob;

    @Test
    void readsEveryXrefRecordAndDisplaysItTwice() throws Exception {
        ListAppender<ILoggingEvent> output = captureOutput(ReadXrefJobConfig.PROGRAM);

        JobExecution execution = run(readXrefJob);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        StepExecution step = execution.getStepExecutions().iterator().next();
        assertThat(step.getReadCount()).isEqualTo(SAMPLE_RECORD_COUNT);

        List<String> lines = recordLines(output, ReadXrefJobConfig.PROGRAM);
        // CBACT03C displays the record in 1000-XREFFILE-GET-NEXT and again in the read loop.
        assertThat(lines).hasSize(SAMPLE_RECORD_COUNT * 2);
        String first = sampleRecord("cardxref.txt", 0, 50);
        assertThat(lines.get(0)).isEqualTo(first);
        assertThat(lines.get(1)).isEqualTo(first);
        assertThat(first).startsWith("050002445376574000000005000000000050");
    }
}
