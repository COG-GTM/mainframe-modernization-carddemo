package com.carddemo.batch.posting;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.domain.Account;
import com.carddemo.domain.Card;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.DailyTransaction;
import com.carddemo.domain.TransactionCategoryBalanceId;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Runs {@code postTranJob} and {@code transactionReportJob} end-to-end against H2 with a small
 * hand-built set of daily transactions, and asserts the faithful {@code CBTRN02C}/{@code CBTRN03C}
 * behaviour: valid transactions are posted, the account balance and cycle credit/debit are
 * updated to the exact expected {@link BigDecimal}, the category balance accumulates, and each
 * invalid transaction is rejected with the correct reason code.
 *
 * <p>Uses a dedicated in-memory database (distinct URL) so it is isolated from the seed-load
 * test's shared H2 instance.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:carddemo-cs11;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class PostTranJobTest {

    private static final String TYPE = "01";
    private static final Integer CAT = 1000;

    private static final String ACCT_A = "99999999901";
    private static final String ACCT_B = "99999999902";
    private static final String ACCT_D = "99999999904";

    private static final String CARD_A = "9999999999999901";
    private static final String CARD_NOACCT = "9999999999999902"; // xref exists, no account -> 101
    private static final String CARD_B = "9999999999999903";
    private static final String CARD_D = "9999999999999904";
    private static final String CARD_UNKNOWN = "0000000000000000"; // no xref -> 100

    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    @Qualifier("postTranJob")
    private Job postTranJob;
    @Autowired
    @Qualifier("transactionReportJob")
    private Job transactionReportJob;

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardXrefRepository cardXrefRepository;
    @Autowired
    private DailyTransactionRepository dailyTransactionRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionCategoryBalanceRepository categoryBalanceRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clean (children before parents to respect FKs).
        jdbcTemplate.update("DELETE FROM posting_transaction_reject");
        transactionRepository.deleteAll();
        categoryBalanceRepository.deleteAll();
        dailyTransactionRepository.deleteAll();
        cardXrefRepository.deleteAll();
        cardRepository.deleteAll();
        accountRepository.deleteAll();

        // Accounts.
        accountRepository.save(account(ACCT_A, "1000.00", "100.00", "50.00", "20.00", "2099-12-31"));
        accountRepository.save(account(ACCT_B, "100.00", "0.00", "0.00", "0.00", "2099-12-31"));
        accountRepository.save(account(ACCT_D, "100000.00", "0.00", "0.00", "0.00", "2000-01-01"));

        // Cards (card_acct_id null; only needed to satisfy the xref->card FK).
        cardRepository.save(card(CARD_A));
        cardRepository.save(card(CARD_NOACCT));
        cardRepository.save(card(CARD_B));
        cardRepository.save(card(CARD_D));

        // Cross references.
        cardXrefRepository.save(xref(CARD_A, ACCT_A));
        cardXrefRepository.save(xref(CARD_NOACCT, null)); // account lookup fails -> reason 101
        cardXrefRepository.save(xref(CARD_B, ACCT_B));
        cardXrefRepository.save(xref(CARD_D, ACCT_D));

        // Daily transactions (processed in ascending id order).
        dailyTransactionRepository.save(daily("CS11TRAN00000001", CARD_A, "200.00", TYPE, CAT, "2024-01-15-10.00.00.000000"));
        dailyTransactionRepository.save(daily("CS11TRAN00000002", CARD_A, "-30.00", TYPE, CAT, "2024-01-16-10.00.00.000000"));
        dailyTransactionRepository.save(daily("CS11TRAN00000003", CARD_UNKNOWN, "10.00", TYPE, CAT, "2024-01-15-10.00.00.000000"));
        dailyTransactionRepository.save(daily("CS11TRAN00000004", CARD_NOACCT, "10.00", TYPE, CAT, "2024-01-15-10.00.00.000000"));
        dailyTransactionRepository.save(daily("CS11TRAN00000005", CARD_B, "500.00", "02", 2000, "2024-01-15-10.00.00.000000"));
        dailyTransactionRepository.save(daily("CS11TRAN00000006", CARD_D, "10.00", TYPE, CAT, "2024-06-01-10.00.00.000000"));
    }

    @Test
    void postsValidTransactionsUpdatesBalancesAndRejectsInvalidWithReason() throws Exception {
        JobExecution execution = jobLauncher.run(postTranJob, new JobParametersBuilder()
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters());
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // Valid transactions posted; invalid ones not written to card_transaction.
        assertThat(transactionRepository.findById("CS11TRAN00000001")).isPresent();
        assertThat(transactionRepository.findById("CS11TRAN00000002")).isPresent();
        assertThat(transactionRepository.findById("CS11TRAN00000003")).isEmpty();
        assertThat(transactionRepository.findById("CS11TRAN00000004")).isEmpty();
        assertThat(transactionRepository.findById("CS11TRAN00000005")).isEmpty();
        assertThat(transactionRepository.findById("CS11TRAN00000006")).isEmpty();
        assertThat(transactionRepository.count()).isEqualTo(2);

        // Posted transactions carry a 26-char DB2-format processing timestamp.
        assertThat(transactionRepository.findById("CS11TRAN00000001").orElseThrow().getTranProcTs())
                .hasSize(26);

        // Account A: currBal 100 + 200 - 30 = 270.00; cyc credit 50 + 200 = 250.00;
        // cyc debit 20 + (-30) = -10.00 (negative amount posts to the cycle debit bucket).
        Account a = accountRepository.findById(ACCT_A).orElseThrow();
        assertThat(a.getAcctCurrBal()).isEqualByComparingTo("270.00");
        assertThat(a.getAcctCurrCycCredit()).isEqualByComparingTo("250.00");
        assertThat(a.getAcctCurrCycDebit()).isEqualByComparingTo("-10.00");

        // Category balance (ACCT_A, "01", 1000): 0 + 200 - 30 = 170.00 (created then updated).
        BigDecimal catBal = categoryBalanceRepository
                .findById(new TransactionCategoryBalanceId(ACCT_A, TYPE, CAT))
                .orElseThrow()
                .getTranCatBal();
        assertThat(catBal).isEqualByComparingTo("170.00");

        // Account B untouched (its only transaction was rejected as overlimit).
        assertThat(accountRepository.findById(ACCT_B).orElseThrow().getAcctCurrBal())
                .isEqualByComparingTo("0.00");

        // Rejects with the exact CBTRN02C reason codes.
        assertThat(jdbcTemplate.queryForObject(reasonSql(), Integer.class, "CS11TRAN00000003")).isEqualTo(100);
        assertThat(jdbcTemplate.queryForObject(reasonSql(), Integer.class, "CS11TRAN00000004")).isEqualTo(101);
        assertThat(jdbcTemplate.queryForObject(reasonSql(), Integer.class, "CS11TRAN00000005")).isEqualTo(102);
        assertThat(jdbcTemplate.queryForObject(reasonSql(), Integer.class, "CS11TRAN00000006")).isEqualTo(103);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM posting_transaction_reject", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT reject_reason_desc FROM posting_transaction_reject WHERE dalytran_id = ?",
                String.class, "CS11TRAN00000005"))
                .isEqualTo("OVERLIMIT TRANSACTION");
    }

    @Test
    void reportJobRendersDetailReportWithExactGrandTotal(@Autowired TransactionReportService reportService)
            throws Exception {
        jobLauncher.run(postTranJob, new JobParametersBuilder()
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters());

        // Direct service assertion: only the two posted transactions (both card CARD_A) fall in
        // range; grand total = 200 - 30 = 170.00.
        TransactionReportService.ReportResult report = reportService.generate("2000-01-01", "2099-12-31");
        assertThat(report.getTransactionCount()).isEqualTo(2);
        assertThat(report.getGrandTotal()).isEqualByComparingTo("170.00");
        assertThat(report.render()).contains("Daily Transaction Report").contains("Grand Total");

        // Report job writes the rendered report to the outputFile parameter.
        Path out = Files.createTempFile("cs11-report", ".txt");
        JobExecution execution = jobLauncher.run(transactionReportJob, new JobParametersBuilder()
                .addString(PostTranJobConfig.RUN_DATE_START, "2000-01-01")
                .addString(PostTranJobConfig.RUN_DATE_END, "2099-12-31")
                .addString(PostTranJobConfig.OUTPUT_FILE, out.toString())
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters());
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(Files.readString(out)).contains("Daily Transaction Report").contains("Grand Total");
    }

    private static String reasonSql() {
        return "SELECT reject_reason_code FROM posting_transaction_reject WHERE dalytran_id = ?";
    }

    private static Account account(String id, String creditLimit, String currBal,
                                   String cycCredit, String cycDebit, String expiration) {
        Account a = new Account();
        a.setAcctId(id);
        a.setAcctActiveStatus("Y");
        a.setAcctCurrBal(new BigDecimal(currBal));
        a.setAcctCreditLimit(new BigDecimal(creditLimit));
        a.setAcctCashCreditLimit(new BigDecimal("0.00"));
        a.setAcctOpenDate("2000-01-01");
        a.setAcctExpirationDate(expiration);
        a.setAcctReissueDate("2000-01-01");
        a.setAcctCurrCycCredit(new BigDecimal(cycCredit));
        a.setAcctCurrCycDebit(new BigDecimal(cycDebit));
        a.setAcctAddrZip("00000");
        a.setAcctGroupId("DEFAULT");
        return a;
    }

    private static Card card(String cardNum) {
        Card c = new Card();
        c.setCardNum(cardNum);
        return c;
    }

    private static CardXref xref(String cardNum, String acctId) {
        CardXref x = new CardXref();
        x.setXrefCardNum(cardNum);
        x.setXrefAcctId(acctId);
        return x;
    }

    private static DailyTransaction daily(String id, String cardNum, String amt, String typeCd,
                                          Integer catCd, String origTs) {
        DailyTransaction t = new DailyTransaction();
        t.setDalytranId(id);
        t.setTranCardNum(cardNum);
        t.setTranTypeCd(typeCd);
        t.setTranCatCd(catCd);
        t.setTranSource("POS");
        t.setTranDesc("CS-11 test transaction");
        t.setTranAmt(new BigDecimal(amt));
        t.setTranMerchantId("000000001");
        t.setTranMerchantName("TEST MERCHANT");
        t.setTranMerchantCity("TEST CITY");
        t.setTranMerchantZip("00000");
        t.setTranOrigTs(origTs);
        t.setTranProcTs("");
        return t;
    }
}
