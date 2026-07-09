package com.carddemo.batch.statement;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.domain.Transaction;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Runs {@code creastmtJob} end-to-end against H2: loads the seed data ({@code dataLoadJob}),
 * adds a couple of transactions for a known card, then generates the statements and asserts the
 * sample account's statement is produced with the expected {@link BigDecimal} total and the key
 * {@code COSTM01} layout fields (balance, FICO, transaction lines, total).
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "carddemo.batch.statement.output-dir=target/test-statements")
class CreateStatementJobTest {

    // Account 00000000001 -> card 9680294154603697, customer 000000001 (Immanuel Madeline Kessler).
    private static final String ACCT_ID = "00000000001";
    private static final String CARD_NUM = "9680294154603697";

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("dataLoadJob")
    private Job dataLoadJob;

    @Autowired
    @Qualifier("creastmtJob")
    private Job creastmtJob;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private StatementItemWriter statementWriter;

    @Test
    void generatesStatementForSampleAccount() throws Exception {
        // 1. Load the seed accounts / customers / cross-references.
        JobExecution load = jobLauncher.run(dataLoadJob, new JobParametersBuilder()
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters());
        assertThat(load.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 2. Add two transactions for the sample account's card: 100.00 + 250.50 = 350.50.
        transactionRepository.save(transaction("0000000000000001", "PURCHASE STORE A", "100.00"));
        transactionRepository.save(transaction("0000000000000002", "PURCHASE STORE B", "250.50"));

        // 3. Generate the statements.
        JobExecution stmt = jobLauncher.run(creastmtJob, new JobParametersBuilder()
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters());
        assertThat(stmt.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 4. Assert the plain-text statement for the sample account.
        List<String> lines = Files.readAllLines(statementWriter.getTextFile(), StandardCharsets.UTF_8);
        assertThat(lines).isNotEmpty();
        assertThat(lines).allSatisfy(l -> assertThat(l).hasSize(StatementFormatter.TEXT_WIDTH));

        int acctLine = indexOfLineStartingWith(lines, "Account ID         :" + ACCT_ID);
        assertThat(acctLine).isGreaterThanOrEqualTo(0);

        // Cardholder name appears above the basic-details block.
        assertThat(lines).anySatisfy(l -> assertThat(l).startsWith("Immanuel Madeline Kessler"));

        // Current Balance : PIC 9(9).99- -> 194.00 with leading zeros + trailing sign space.
        assertThat(lines.get(acctLine + 1))
                .isEqualTo(pad("Current Balance    :000000194.00 "));
        // FICO Score : PIC X(20) left-justified.
        assertThat(lines.get(acctLine + 2)).startsWith("FICO Score         :274");

        // Transaction lines: TRNX-ID(16) ' ' TRNX-DESC(49) '$' TRNX-AMT Z(9).99-.
        assertThat(lines).anySatisfy(l -> {
            assertThat(l).startsWith("0000000000000001 PURCHASE STORE A");
            assertThat(l).endsWith("$      100.00 ");
        });
        assertThat(lines).anySatisfy(l -> {
            assertThat(l).startsWith("0000000000000002 PURCHASE STORE B");
            assertThat(l).endsWith("$      250.50 ");
        });

        // Total EXP: PIC Z(9).99- -> 350.50 (this exact total is unique to the sample account).
        assertThat(lines).contains(pad("Total EXP:" + " ".repeat(56) + "$      350.50 "));

        // 5. Assert the HTML statement contains the account header and a transaction.
        String html = Files.readString(statementWriter.getHtmlFile(), StandardCharsets.UTF_8);
        assertThat(html).contains("<h3>Statement for Account Number: " + ACCT_ID + "</h3>");
        assertThat(html).contains("<p>PURCHASE STORE A</p>");
        assertThat(html).contains("<h3>End of Statement</h3>");
    }

    private Transaction transaction(String id, String desc, String amount) {
        Transaction t = new Transaction();
        t.setTranId(id);
        t.setTranCardNum(CARD_NUM);
        t.setTranDesc(desc);
        t.setTranAmt(new BigDecimal(amount));
        t.setTranTypeCd("01");
        t.setTranCatCd(1);
        return t;
    }

    private static int indexOfLineStartingWith(List<String> lines, String prefix) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(prefix)) {
                return i;
            }
        }
        return -1;
    }

    private static String pad(String s) {
        return s + " ".repeat(StatementFormatter.TEXT_WIDTH - s.length());
    }
}
