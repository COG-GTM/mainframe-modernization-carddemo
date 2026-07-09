package com.carddemo.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.batch.orchestration.BatchJobLaunchResult;
import com.carddemo.batch.orchestration.BatchJobLauncherService;
import com.carddemo.batch.statement.StatementFormatter;
import com.carddemo.batch.statement.StatementItemWriter;
import com.carddemo.domain.Account;
import com.carddemo.domain.Card;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Customer;
import com.carddemo.domain.DailyTransaction;
import com.carddemo.domain.DisclosureGroup;
import com.carddemo.domain.DisclosureGroupId;
import com.carddemo.domain.Transaction;
import com.carddemo.domain.TransactionCategoryBalanceId;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * CS-15 end-to-end <em>batch golden path</em>: launches the three CS-14 monthly/daily pipelines
 * back-to-back through the real {@link BatchJobLauncherService} (the same code path the REST /
 * CLI / scheduler launchers use) against a deterministic hand-built fixture, and asserts each
 * pipeline reaches {@code COMPLETED} <em>and</em> the numeric DB state that results when the data
 * flows through all three, chaining the exact {@link BigDecimal} arithmetic of the legacy
 * programs:
 *
 * <ol>
 *   <li>{@code posttran} (POSTTRAN.jcl → {@code CBTRN02C}) posts the daily transactions, updating
 *       balances/category balances and rejecting the unknown card (reason 100);</li>
 *   <li>{@code intcalc} (INTCALC.jcl → {@code CBACT04C}) computes monthly interest on the posted
 *       category balance, adds it to the account balance and zeroes the cycle buckets;</li>
 *   <li>{@code creastmt} (CREASTMT.JCL → {@code CBSTM03A}) prints the statement, whose current
 *       balance and transaction total reflect the posting + interest just applied.</li>
 * </ol>
 *
 * <p>The fixture and its expected values are self-contained (a dedicated in-memory schema), so the
 * arithmetic can be verified end-to-end without depending on the shared seed. Every expected
 * constant cites the COBOL source that produces it.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:carddemo-cs15-batch;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "carddemo.batch.statement.output-dir=target/cs15-e2e-statements"
})
class BatchGoldenPathE2ETest {

    private static final String TYPE = "01";
    private static final Integer CAT = 1000;
    private static final String GROUP = "DEFAULT";

    private static final String ACCT = "77777777701";
    private static final String CARD = "7777777777777701";
    private static final String CUST = "777000001";
    private static final String CARD_UNKNOWN = "0000000000000000"; // no xref → reject 100

    @Autowired
    private BatchJobLauncherService launcher;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CustomerRepository customerRepository;
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
    private DisclosureGroupRepository disclosureGroupRepository;
    @Autowired
    private StatementItemWriter statementWriter;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM posting_transaction_reject");
        transactionRepository.deleteAll();
        categoryBalanceRepository.deleteAll();
        dailyTransactionRepository.deleteAll();
        cardXrefRepository.deleteAll();
        cardRepository.deleteAll();
        disclosureGroupRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();

        // Account starts at 100.00 balance, cycle credit 50.00 / debit 20.00, group DEFAULT.
        customerRepository.save(customer());
        accountRepository.save(account());
        cardRepository.save(card());
        cardXrefRepository.save(xref());

        // DEFAULT disclosure rate for (type 01, cat 1000) = 12.00% annual.
        disclosureGroupRepository.save(disc(GROUP, TYPE, CAT, "12.00"));

        // Daily file: two valid postings for CARD and one for an unknown card (rejected).
        dailyTransactionRepository.save(daily("CS15GP0000000001", CARD, "200.00"));
        dailyTransactionRepository.save(daily("CS15GP0000000002", CARD, "-30.00"));
        dailyTransactionRepository.save(daily("CS15GP0000000003", CARD_UNKNOWN, "10.00"));
    }

    @Test
    void runsPostTranThenIntcalcThenCreateStatementEndToEnd() throws Exception {
        // ---- 1. POSTTRAN (CBTRN02C) ----------------------------------------------------------
        BatchJobLaunchResult post = launcher.launch("posttran",
                Map.of("startDate", "2000-01-01", "endDate", "2099-12-31"));
        assertThat(post.getPipeline()).isEqualTo("posttran");
        assertThat(post.getStatus()).isEqualTo(BatchStatus.COMPLETED.toString());

        // Two valid transactions posted; unknown card rejected (CBTRN02C 1500-A-LOOKUP-XREF → 100).
        assertThat(transactionRepository.count()).isEqualTo(2);
        Integer rejects = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM posting_transaction_reject", Integer.class);
        assertThat(rejects).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT reject_reason_code FROM posting_transaction_reject WHERE dalytran_id = ?",
                Integer.class, "CS15GP0000000003")).isEqualTo(100);

        // CBTRN02C 2700-UPDATE-ACCOUNT: ADD DALYTRAN-AMT TO ACCT-CURR-BAL (100 + 200 - 30 = 270.00);
        // positive amount → ACCT-CURR-CYC-CREDIT (50 + 200 = 250.00); negative → ACCT-CURR-CYC-DEBIT
        // (20 + (-30) = -10.00).
        Account afterPost = accountRepository.findById(ACCT).orElseThrow();
        assertThat(afterPost.getAcctCurrBal()).isEqualByComparingTo("270.00");
        assertThat(afterPost.getAcctCurrCycCredit()).isEqualByComparingTo("250.00");
        assertThat(afterPost.getAcctCurrCycDebit()).isEqualByComparingTo("-10.00");

        // CBTRN02C 2500-A/B: ADD DALYTRAN-AMT TO TRAN-CAT-BAL → 0 + 200 - 30 = 170.00.
        BigDecimal catBal = categoryBalanceRepository
                .findById(new TransactionCategoryBalanceId(ACCT, TYPE, CAT))
                .orElseThrow().getTranCatBal();
        assertThat(catBal).isEqualByComparingTo("170.00");

        // ---- 2. INTCALC (CBACT04C) -----------------------------------------------------------
        BatchJobLaunchResult interest = launcher.launch("intcalc",
                Map.of("parmDate", "2026-07-09"));
        assertThat(interest.getPipeline()).isEqualTo("intcalc");
        assertThat(interest.getStatus()).isEqualTo(BatchStatus.COMPLETED.toString());

        // CBACT04C 1300-COMPUTE-INTEREST: WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200,
        // WS-MONTHLY-INT PIC S9(9)V99 (no ROUNDED → truncation): 170.00 * 12.00 / 1200 = 1.70.
        // 1120-UPDATE-ACCOUNT: ADD WS-TOTAL-INT TO ACCT-CURR-BAL (270.00 + 1.70 = 271.70), then
        // MOVE 0 TO ACCT-CURR-CYC-CREDIT / ACCT-CURR-CYC-DEBIT.
        Account afterInterest = accountRepository.findById(ACCT).orElseThrow();
        assertThat(afterInterest.getAcctCurrBal()).isEqualByComparingTo("271.70");
        assertThat(afterInterest.getAcctCurrBal().scale()).isEqualTo(2);
        assertThat(afterInterest.getAcctCurrCycCredit()).isEqualByComparingTo("0.00");
        assertThat(afterInterest.getAcctCurrCycDebit()).isEqualByComparingTo("0.00");

        // One interest transaction for the card, amount 1.70 (CBACT04C 1300-B-WRITE-TX).
        List<Transaction> interestTxns = transactionRepository.findByTranCardNum(CARD).stream()
                .filter(t -> ("Int. for a/c " + ACCT).equals(t.getTranDesc())).toList();
        assertThat(interestTxns).hasSize(1);
        assertThat(interestTxns.get(0).getTranAmt()).isEqualByComparingTo("1.70");

        // ---- 3. CREASTMT (CBSTM03A) ----------------------------------------------------------
        BatchJobLaunchResult stmt = launcher.launch("creastmt");
        assertThat(stmt.getPipeline()).isEqualTo("creastmt");
        assertThat(stmt.getStatus()).isEqualTo(BatchStatus.COMPLETED.toString());

        List<String> lines = Files.readAllLines(statementWriter.getTextFile(), StandardCharsets.UTF_8);
        assertThat(lines).isNotEmpty();
        assertThat(lines).allSatisfy(l -> assertThat(l).hasSize(StatementFormatter.TEXT_WIDTH));

        int acctLine = indexOfLineStartingWith(lines, "Account ID         :" + ACCT);
        assertThat(acctLine).isGreaterThanOrEqualTo(0);
        // ST-CURR-BAL PIC 9(9).99- of the post-interest balance 271.70.
        assertThat(lines.get(acctLine + 1)).isEqualTo(pad("Current Balance    :000000271.70 "));
        // ST-TOTAL-TRAMT Z(9).99-: 200.00 + (-30.00) + 1.70 = 171.70 (all three posted txns).
        assertThat(lines).contains(pad("Total EXP:" + " ".repeat(56) + "$      171.70 "));
    }

    // --- fixture builders ----------------------------------------------------------------------

    private static Customer customer() {
        Customer c = new Customer();
        c.setCustId(CUST);
        c.setCustFirstName("GRACE");
        c.setCustMiddleName("ADA");
        c.setCustLastName("HOPPER");
        c.setCustAddrLine1("1 Navy Yard");
        c.setCustAddrStateCd("DC");
        c.setCustAddrCountryCd("USA");
        c.setCustAddrZip("20374");
        c.setCustFicoCreditScore(800);
        return c;
    }

    private static Account account() {
        Account a = new Account();
        a.setAcctId(ACCT);
        a.setAcctActiveStatus("Y");
        a.setAcctCurrBal(new BigDecimal("100.00"));
        a.setAcctCreditLimit(new BigDecimal("5000.00"));
        a.setAcctCashCreditLimit(new BigDecimal("1000.00"));
        a.setAcctOpenDate("2000-01-01");
        a.setAcctExpirationDate("2099-12-31");
        a.setAcctReissueDate("2000-01-01");
        a.setAcctCurrCycCredit(new BigDecimal("50.00"));
        a.setAcctCurrCycDebit(new BigDecimal("20.00"));
        a.setAcctAddrZip("20374");
        a.setAcctGroupId(GROUP);
        return a;
    }

    private static Card card() {
        Card c = new Card();
        c.setCardNum(CARD);
        c.setCardAcctId(ACCT);
        c.setCardActiveStatus("Y");
        return c;
    }

    private static CardXref xref() {
        CardXref x = new CardXref();
        x.setXrefCardNum(CARD);
        x.setXrefCustId(CUST);
        x.setXrefAcctId(ACCT);
        return x;
    }

    private static DisclosureGroup disc(String group, String type, int cat, String rate) {
        DisclosureGroup d = new DisclosureGroup();
        d.setId(new DisclosureGroupId(group, type, cat));
        d.setDisIntRate(new BigDecimal(rate));
        return d;
    }

    private static DailyTransaction daily(String id, String cardNum, String amt) {
        DailyTransaction t = new DailyTransaction();
        t.setDalytranId(id);
        t.setTranCardNum(cardNum);
        t.setTranTypeCd(TYPE);
        t.setTranCatCd(CAT);
        t.setTranSource("POS");
        t.setTranDesc("CS-15 golden path");
        t.setTranAmt(new BigDecimal(amt));
        t.setTranMerchantId("000000001");
        t.setTranMerchantName("TEST MERCHANT");
        t.setTranMerchantCity("TEST CITY");
        t.setTranMerchantZip("00000");
        t.setTranOrigTs("2024-01-15-10.00.00.000000");
        t.setTranProcTs("");
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
