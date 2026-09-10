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

/** Verifies the CBACT02C migration (JCL READCARD) against the sample CARDDATA extract. */
class ReadCardJobConfigTest extends AbstractReaderJobTest {

    @Autowired
    @Qualifier(ReadCardJobConfig.JOB_NAME)
    private Job readCardJob;

    @Test
    void readsEveryCardRecordAndDisplaysItOnce() throws Exception {
        ListAppender<ILoggingEvent> output = captureOutput(ReadCardJobConfig.PROGRAM);

        JobExecution execution = run(readCardJob);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        StepExecution step = execution.getStepExecutions().iterator().next();
        assertThat(step.getReadCount()).isEqualTo(SAMPLE_RECORD_COUNT);
        assertThat(step.getWriteCount()).isEqualTo(SAMPLE_RECORD_COUNT);

        List<String> lines = recordLines(output, ReadCardJobConfig.PROGRAM);
        // The DISPLAY in 1000-CARDFILE-GET-NEXT is commented out: one line per record.
        assertThat(lines).hasSize(SAMPLE_RECORD_COUNT);
        assertThat(lines.get(0)).isEqualTo(sampleRecord("carddata.txt", 0, 150));
        assertThat(lines.get(0)).startsWith("050002445376574000000000050747Aniya Von");
        assertThat(lines.get(SAMPLE_RECORD_COUNT - 1))
                .isEqualTo(sampleRecord("carddata.txt", SAMPLE_RECORD_COUNT - 1, 150));
    }
}
