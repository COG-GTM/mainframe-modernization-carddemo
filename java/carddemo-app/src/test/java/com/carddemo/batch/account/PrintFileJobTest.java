package com.carddemo.batch.account;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Runs the four read/print jobs ({@code accountFileJob}/{@code CBACT01C},
 * {@code cardFileJob}/{@code CBACT02C}, {@code xrefFileJob}/{@code CBACT03C},
 * {@code customerFileJob}/{@code CBCUS01C}) against H2 seeded via {@code dataLoadJob}, and
 * asserts each job writes exactly one output line per source record.
 */
@SpringBootTest
@ActiveProfiles("test")
class PrintFileJobTest {

    @TempDir
    Path tempDir;

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job dataLoadJob;
    @Autowired
    private Job accountFileJob;
    @Autowired
    private Job cardFileJob;
    @Autowired
    private Job xrefFileJob;
    @Autowired
    private Job customerFileJob;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardXrefRepository cardXrefRepository;
    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void loadSeedData() throws Exception {
        JobExecution execution = jobLauncher.run(dataLoadJob, new JobParametersBuilder()
                .addLong("run", System.nanoTime())
                .toJobParameters());
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void accountFileJobPrintsOneLinePerAccount() throws Exception {
        assertLineCountMatches(accountFileJob, "acctfile.txt", accountRepository.count());
    }

    @Test
    void cardFileJobPrintsOneLinePerCard() throws Exception {
        assertLineCountMatches(cardFileJob, "cardfile.txt", cardRepository.count());
    }

    @Test
    void xrefFileJobPrintsOneLinePerXref() throws Exception {
        assertLineCountMatches(xrefFileJob, "xreffile.txt", cardXrefRepository.count());
    }

    @Test
    void customerFileJobPrintsOneLinePerCustomer() throws Exception {
        assertLineCountMatches(customerFileJob, "custfile.txt", customerRepository.count());
    }

    private void assertLineCountMatches(Job job, String fileName, long expected) throws Exception {
        assertThat(expected).isPositive();
        Path output = tempDir.resolve(fileName);
        JobExecution execution = jobLauncher.run(job, new JobParametersBuilder()
                .addString("outputFile", output.toString())
                .addLong("run", System.nanoTime())
                .toJobParameters());

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(Files.exists(output)).isTrue();
        long lines = Files.lines(output).filter(l -> !l.isBlank()).count();
        assertThat(lines).isEqualTo(expected);
    }
}
