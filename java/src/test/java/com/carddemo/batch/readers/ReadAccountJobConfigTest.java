package com.carddemo.batch.readers;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.carddemo.model.entity.Account;
import com.carddemo.repository.AccountRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/** Verifies the CBACT01C migration (JCL READACCT) against the sample ACCTDATA extract. */
class ReadAccountJobConfigTest extends AbstractReaderJobTest {

    @Autowired
    @Qualifier(ReadAccountJobConfig.JOB_NAME)
    private Job readAccountJob;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void readsEveryAccountRecordInKeyOrder() throws Exception {
        ListAppender<ILoggingEvent> output = captureOutput(ReadAccountJobConfig.PROGRAM);

        JobExecution execution = run(readAccountJob);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        StepExecution step = execution.getStepExecutions().iterator().next();
        assertThat(step.getReadCount()).isEqualTo(SAMPLE_RECORD_COUNT);
        assertThat(step.getWriteCount()).isEqualTo(SAMPLE_RECORD_COUNT);

        List<String> lines = recordLines(output, ReadAccountJobConfig.PROGRAM);
        // 11 labelled lines + separator + the group level DISPLAY, per record.
        assertThat(lines).hasSize(SAMPLE_RECORD_COUNT * 13);
        assertThat(lines.get(0)).isEqualTo("ACCT-ID                 :00000000001");
        assertThat(lines.get(2)).isEqualTo("ACCT-CURR-BAL           :00000001940{");
        assertThat(lines.get(11)).isEqualTo("-".repeat(49));
        assertThat(lines.get(12)).isEqualTo(sampleRecord("acctdata.txt", 0, 300));
    }

    @Test
    void formatsTheDisplayParagraphOfASingleRecord() {
        Account account = accountRepository.findById(1L).orElseThrow();
        account.setCurrentBalance(new BigDecimal("-1234.56"));
        account.setGroupId("ZEROAPR");

        List<String> lines = Cbact01cDisplay.displayLines(account);

        assertThat(lines.get(1)).isEqualTo("ACCT-ACTIVE-STATUS      :Y");
        // S9(10)V99 with a negative overpunch on the last digit: -1234.56 -> ...12345O
        assertThat(lines.get(2)).isEqualTo("ACCT-CURR-BAL           :00000012345O");
        assertThat(lines.get(5)).isEqualTo("ACCT-OPEN-DATE          :2014-11-20");
        assertThat(lines.get(10)).isEqualTo("ACCT-GROUP-ID           :ZEROAPR   ");
        assertThat(lines.get(12)).hasSize(300);
    }
}
