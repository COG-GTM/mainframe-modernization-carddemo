package com.carddemo.batch.readers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;

/** Launching the reader jobs by name, the way the JCL EXEC PGM= steps do. */
class SequentialReaderJobLauncherTest extends AbstractReaderJobTest {

    @Autowired
    private SequentialReaderJobLauncher launcher;

    @Test
    void exposesEveryReaderJobByName() {
        assertThat(launcher.jobNames()).contains(ReadAccountJobConfig.JOB_NAME,
                ReadCardJobConfig.JOB_NAME, ReadXrefJobConfig.JOB_NAME,
                ReadCustomerJobConfig.JOB_NAME);
    }

    @Test
    void launchesAJobByName() throws Exception {
        assertThat(launcher.launch(ReadCardJobConfig.JOB_NAME).getStatus())
                .isEqualTo(BatchStatus.COMPLETED);
    }

    /** Step scoped readers: two overlapping submissions must each read the whole file. */
    @Test
    void overlappingLaunchesEachReadEveryRecord() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<JobExecution>> executions = executor.invokeAll(List.of(
                    () -> launcher.launch(ReadCardJobConfig.JOB_NAME),
                    () -> launcher.launch(ReadCardJobConfig.JOB_NAME)));
            for (Future<JobExecution> future : executions) {
                JobExecution execution = future.get();
                assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
                assertThat(execution.getStepExecutions()).allSatisfy(step ->
                        assertThat(step.getWriteCount()).isEqualTo(SAMPLE_RECORD_COUNT));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void rejectsAnUnknownJobName() {
        assertThatThrownBy(() -> launcher.launch("readNothingJob"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("readNothingJob");
    }
}
