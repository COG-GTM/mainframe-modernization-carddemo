package com.carddemo.batch.readers;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.carddemo.data.SeedDataLoader;
import com.carddemo.util.CobolUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Shared harness for the sequential VSAM reader jobs (CBACT01C, CBACT02C, CBACT03C,
 * CBCUS01C): loads the sample extracts from {@code app/data/ASCII} and runs a single job
 * against them, so the job output can be compared with the original fixed-width records.
 */
@SpringBootTest
@TestPropertySource(properties = "carddemo.data.load-on-startup=false")
abstract class AbstractReaderJobTest {

    /** Number of records in every sample extract shipped with the COBOL application. */
    static final int SAMPLE_RECORD_COUNT = 50;

    private static final Path DATA_DIRECTORY = Path.of("../app/data/ASCII");

    @Autowired
    private SeedDataLoader seedDataLoader;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRepository jobRepository;

    @BeforeEach
    void loadSampleData() throws IOException {
        seedDataLoader.load();
    }

    JobExecution run(Job job) throws Exception {
        JobLauncherTestUtils utils = new JobLauncherTestUtils();
        utils.setJob(job);
        utils.setJobLauncher(jobLauncher);
        utils.setJobRepository(jobRepository);
        return utils.launchJob(utils.getUniqueJobParameters());
    }

    /** A record of a sample extract, padded to the copybook record length. */
    static String sampleRecord(String fileName, int index, int recordLength) throws IOException {
        List<String> lines = Files.readAllLines(DATA_DIRECTORY.resolve(fileName),
                StandardCharsets.ISO_8859_1);
        return CobolUtils.padRight(lines.get(index), recordLength);
    }

    /** Captures the SYSOUT lines the job logs under the COBOL program name. */
    static ListAppender<ILoggingEvent> captureOutput(String program) {
        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(program);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.setLevel(Level.INFO);
        logger.addAppender(appender);
        return appender;
    }

    static List<String> lines(ListAppender<ILoggingEvent> appender) {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    /**
     * The record lines, i.e. everything between the START and END OF EXECUTION DISPLAYs of
     * the program's PROCEDURE DIVISION.
     */
    static List<String> recordLines(ListAppender<ILoggingEvent> appender, String program) {
        List<String> all = lines(appender);
        assertThat(all.get(0)).isEqualTo("START OF EXECUTION OF PROGRAM " + program);
        assertThat(all.get(all.size() - 1)).isEqualTo("END OF EXECUTION OF PROGRAM " + program);
        return all.subList(1, all.size() - 1);
    }
}
