package com.carddemo.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.batch.orchestration.BatchJobLaunchResult;
import com.carddemo.batch.orchestration.BatchJobLauncherService;
import com.carddemo.domain.Account;
import com.carddemo.domain.Card;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Customer;
import com.carddemo.domain.DailyTransaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import java.math.BigDecimal;
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
 * CS-15 numeric validation of the {@code CBTRN02C} posting rejection logic
 * ({@code 1500-A-LOOKUP-XREF} / {@code 1500-B-LOOKUP-ACCT}, app/cbl/CBTRN02C.cbl lines 380-422),
 * driven end-to-end through the real {@code posttran} pipeline against a deterministic fixture.
 *
 * <p>Reject reason codes are asserted exactly as the COBOL assigns them:</p>
 * <ul>
 *   <li><b>100</b> INVALID CARD NUMBER — card absent from the XREF (line 385);</li>
 *   <li><b>101</b> ACCOUNT NOT FOUND — XREF resolves but the account is missing (line 397);</li>
 *   <li><b>102</b> OVERLIMIT — {@code WS-TEMP-BAL = CYC-CREDIT - CYC-DEBIT + DALYTRAN-AMT}, rejected
 *       iff {@code ACCT-CREDIT-LIMIT < WS-TEMP-BAL} (lines 403-413);</li>
 *   <li><b>103</b> AFTER EXPIRATION — {@code ACCT-EXPIRAION-DATE < DALYTRAN-ORIG-TS(1:10)}
 *       (lines 414-420).</li>
 * </ul>
 *
 * <p>The overlimit boundary is pinned precisely: a transaction that brings {@code WS-TEMP-BAL}
 * <em>exactly to</em> the credit limit is posted ({@code >=} is satisfied), while one cent more
 * is rejected 102.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:carddemo-cs15-reject;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class PostingRejectNumericValidationE2ETest {

    private static final String TYPE = "01";
    private static final Integer CAT = 1000;

    @Autowired private BatchJobLauncherService launcher;
    @Autowired private AccountRepository accountRepository;
    @Autowired private CardRepository cardRepository;
    @Autowired private CardXrefRepository cardXrefRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private DailyTransactionRepository dailyTransactionRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private TransactionCategoryBalanceRepository categoryBalanceRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM posting_transaction_reject");
        transactionRepository.deleteAll();
        categoryBalanceRepository.deleteAll();
        dailyTransactionRepository.deleteAll();
        cardXrefRepository.deleteAll();
        cardRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();

        // Single cardholder referenced by every XREF (satisfies the XREF→CUSTOMER FK).
        Customer holder = new Customer();
        holder.setCustId("000000001");
        holder.setCustFirstName("TEST");
        holder.setCustLastName("HOLDER");
        customerRepository.save(holder);

        // (a) at-limit account: credit limit 1000.00, cycle 0/0 → WS-TEMP-BAL == limit → posted.
        seedAccountCardXref("10000000001", "1000000000000001", "1000.00", "0.00", "0.00", "2099-12-31");
        // (b) overlimit-by-one-cent account: same limit, txn one cent higher → reason 102.
        seedAccountCardXref("10000000002", "1000000000000002", "1000.00", "0.00", "0.00", "2099-12-31");
        // (c) expired account (limit ample) → reason 103.
        seedAccountCardXref("10000000003", "1000000000000003", "9999.00", "0.00", "0.00", "2020-12-31");
        // (d) card resolves in XREF but the account lookup fails → reason 101. The nullable
        // FK_CARD_ACCOUNT / xref account id are left null so no account row is referenced.
        Card orphanCard = new Card();
        orphanCard.setCardNum("1000000000000004");
        orphanCard.setCardActiveStatus("Y");
        cardRepository.save(orphanCard);
        CardXref orphan = new CardXref();
        orphan.setXrefCardNum("1000000000000004");
        orphan.setXrefCustId("000000001");
        cardXrefRepository.save(orphan);

        dailyTransactionRepository.save(daily("PN00000000000001", "1000000000000001", "1000.00")); // posted
        dailyTransactionRepository.save(daily("PN00000000000002", "1000000000000002", "1000.01")); // 102
        dailyTransactionRepository.save(daily("PN00000000000003", "1000000000000003", "5.00"));     // 103
        dailyTransactionRepository.save(daily("PN00000000000004", "1000000000000004", "5.00"));     // 101
        dailyTransactionRepository.save(daily("PN00000000000005", "9999999999999999", "5.00"));     // 100
    }

    @Test
    void assignsExactRejectReasonCodesAndBoundary() {
        BatchJobLaunchResult post = launcher.launch("posttran",
                Map.of("startDate", "2000-01-01", "endDate", "2099-12-31"));
        assertThat(post.getStatus()).isEqualTo(BatchStatus.COMPLETED.toString());

        // Exactly one transaction posted (the at-limit account); four rejected.
        assertThat(transactionRepository.count()).isEqualTo(1);
        assertThat(rejectCode("PN00000000000002")).isEqualTo(102);
        assertThat(rejectCode("PN00000000000003")).isEqualTo(103);
        assertThat(rejectCode("PN00000000000004")).isEqualTo(101);
        assertThat(rejectCode("PN00000000000005")).isEqualTo(100);
        Integer rejects = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM posting_transaction_reject", Integer.class);
        assertThat(rejects).isEqualTo(4);

        // At-limit account posted: 0.00 + 1000.00 = 1000.00; overlimit account untouched (0.00).
        assertThat(accountRepository.findById("10000000001").orElseThrow().getAcctCurrBal())
                .isEqualByComparingTo("1000.00");
        assertThat(accountRepository.findById("10000000002").orElseThrow().getAcctCurrBal())
                .isEqualByComparingTo("0.00");
    }

    private Integer rejectCode(String dalytranId) {
        return jdbcTemplate.queryForObject(
                "SELECT reject_reason_code FROM posting_transaction_reject WHERE dalytran_id = ?",
                Integer.class, dalytranId);
    }

    private void seedAccountCardXref(String acctId, String cardNum, String creditLimit,
                                     String cycCredit, String cycDebit, String expiration) {
        Account a = new Account();
        a.setAcctId(acctId);
        a.setAcctActiveStatus("Y");
        a.setAcctCurrBal(new BigDecimal("0.00"));
        a.setAcctCreditLimit(new BigDecimal(creditLimit));
        a.setAcctCashCreditLimit(new BigDecimal("1000.00"));
        a.setAcctOpenDate("2000-01-01");
        a.setAcctExpirationDate(expiration);
        a.setAcctReissueDate("2000-01-01");
        a.setAcctCurrCycCredit(new BigDecimal(cycCredit));
        a.setAcctCurrCycDebit(new BigDecimal(cycDebit));
        a.setAcctAddrZip("00000");
        a.setAcctGroupId("DEFAULT");
        accountRepository.save(a);

        Card c = new Card();
        c.setCardNum(cardNum);
        c.setCardAcctId(acctId);
        c.setCardActiveStatus("Y");
        cardRepository.save(c);

        CardXref x = new CardXref();
        x.setXrefCardNum(cardNum);
        x.setXrefCustId("000000001");
        x.setXrefAcctId(acctId);
        cardXrefRepository.save(x);
    }

    private static DailyTransaction daily(String id, String cardNum, String amt) {
        DailyTransaction t = new DailyTransaction();
        t.setDalytranId(id);
        t.setTranCardNum(cardNum);
        t.setTranTypeCd(TYPE);
        t.setTranCatCd(CAT);
        t.setTranSource("POS");
        t.setTranDesc("CS-15 reject validation");
        t.setTranAmt(new BigDecimal(amt));
        t.setTranMerchantId("000000001");
        t.setTranMerchantName("TEST MERCHANT");
        t.setTranMerchantCity("TEST CITY");
        t.setTranMerchantZip("00000");
        t.setTranOrigTs("2024-01-15-10.00.00.000000");
        t.setTranProcTs("");
        return t;
    }
}
