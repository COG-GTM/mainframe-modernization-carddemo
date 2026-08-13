package com.carddemo.batch.readers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
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

    @Test
    void rejectsAnUnknownJobName() {
        assertThatThrownBy(() -> launcher.launch("readNothingJob"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("readNothingJob");
    }
}
