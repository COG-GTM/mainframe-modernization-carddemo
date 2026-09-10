package com.carddemo.batch.readers;

import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;

/**
 * COBOL programs: CBACT01C, CBACT02C, CBACT03C, CBCUS01C — the parts every sequential VSAM
 * reader program has in common: the {@code START/END OF EXECUTION} DISPLAYs, the SYSOUT
 * writer, the sequential (key ordered) read of the file, and the
 * {@code 9910-DISPLAY-IO-STATUS} / {@code 9999-ABEND-PROGRAM} error path.
 */
final class VsamReaderJobSupport {

    /**
     * File status reported when the relational read fails. COBOL propagates the VSAM status
     * byte pair; the JPA reader has no equivalent, so the generic "no further information"
     * VSAM I/O status 90 is used, which the 9910 paragraph renders as {@code 9048}.
     */
    private static final String IO_ERROR_STATUS = "90";

    private static final int PAGE_SIZE = 100;

    private VsamReaderJobSupport() {
    }

    /** Sequential read of a KSDS: every row of the table, in ascending key order. */
    static <T> JpaPagingItemReader<T> reader(String name,
                                             EntityManagerFactory entityManagerFactory,
                                             String jpql) {
        return new JpaPagingItemReaderBuilder<T>()
                .name(name)
                .entityManagerFactory(entityManagerFactory)
                .queryString(jpql)
                .pageSize(PAGE_SIZE)
                .saveState(false)
                .build();
    }

    /** Writes each record to the log the way the program DISPLAYs it to SYSOUT. */
    static <T> ItemWriter<T> displayWriter(String program, Function<T, List<String>> display) {
        Logger log = LoggerFactory.getLogger(program);
        return items -> {
            for (T item : items) {
                display.apply(item).forEach(log::info);
            }
        };
    }

    /**
     * {@code DISPLAY 'START OF EXECUTION ...'} / {@code 'END OF EXECUTION ...'}. A failed step
     * takes the COBOL error path instead: the read error message, the file status and
     * {@code ABENDING PROGRAM}; the job itself ends FAILED, which replaces the CEE3ABD abend.
     * A job stopped by an operator has no COBOL equivalent and does not take the error path.
     */
    static JobExecutionListener executionListener(String program, String readErrorMessage) {
        Logger log = LoggerFactory.getLogger(program);
        return new JobExecutionListener() {

            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("START OF EXECUTION OF PROGRAM {}", program);
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                    log.info("END OF EXECUTION OF PROGRAM {}", program);
                    return;
                }
                if (jobExecution.getStatus() == BatchStatus.STOPPED) {
                    log.warn("EXECUTION OF PROGRAM {} STOPPED", program);
                    return;
                }
                jobExecution.getAllFailureExceptions()
                        .forEach(failure -> log.error(program + " failed", failure));
                log.error(readErrorMessage);
                log.error(CobolDisplay.ioStatus(IO_ERROR_STATUS));
                log.error("ABENDING PROGRAM");
            }
        };
    }
}
