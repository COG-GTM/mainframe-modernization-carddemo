package com.carddemo.batch.orchestration;

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
 * Launches the CS-14 {@code postTranPipelineJob} end-to-end against H2 through
 * {@link BatchJobLauncherService} (the same path the REST endpoint uses) and asserts the pipeline
 * completes and the underlying POSTTRAN effects occurred: transactions posted, balances updated to
 * the exact {@link BigDecimal}, category balance accumulated and the invalid record rejected.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:carddemo-cs14-posttran;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class PostTranPipelineJobTest {

    private static final String TYPE = "01";
    private static final Integer CAT = 1000;
    private static final String ACCT_A = "88888888801";
    private static final String CARD_A = "8888888888888801";
    private static final String CARD_UNKNOWN = "0000000000000000"; // no xref -> reject 100

    @Autowired
    private BatchJobLauncherService launcher;
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
        jdbcTemplate.update("DELETE FROM posting_transaction_reject");
        transactionRepository.deleteAll();
        categoryBalanceRepository.deleteAll();
        dailyTransactionRepository.deleteAll();
        cardXrefRepository.deleteAll();
        cardRepository.deleteAll();
        accountRepository.deleteAll();

        accountRepository.save(account(ACCT_A, "1000.00", "100.00", "50.00", "20.00"));
        cardRepository.save(card(CARD_A));
        cardXrefRepository.save(xref(CARD_A, ACCT_A));

        dailyTransactionRepository.save(daily("CS14PIPE00000001", CARD_A, "200.00", "2024-01-15-10.00.00.000000"));
        dailyTransactionRepository.save(daily("CS14PIPE00000002", CARD_A, "-30.00", "2024-01-16-10.00.00.000000"));
        dailyTransactionRepository.save(daily("CS14PIPE00000003", CARD_UNKNOWN, "10.00", "2024-01-15-10.00.00.000000"));
    }

    @Test
    void runsFullPostTranPipelineAndPostsTransactions() {
        BatchJobLaunchResult result = launcher.launch("posttran",
                Map.of("startDate", "2000-01-01", "endDate", "2099-12-31"));

        assertThat(result.getPipeline()).isEqualTo("posttran");
        assertThat(result.getStatus()).isEqualTo(BatchStatus.COMPLETED.toString());

        // Two valid transactions posted; the unknown card was rejected (reason 100).
        assertThat(transactionRepository.count()).isEqualTo(2);

        Account a = accountRepository.findById(ACCT_A).orElseThrow();
        assertThat(a.getAcctCurrBal()).isEqualByComparingTo("270.00");   // 100 + 200 - 30
        assertThat(a.getAcctCurrCycCredit()).isEqualByComparingTo("250.00"); // 50 + 200
        assertThat(a.getAcctCurrCycDebit()).isEqualByComparingTo("-10.00");  // 20 + (-30)

        BigDecimal catBal = categoryBalanceRepository
                .findById(new TransactionCategoryBalanceId(ACCT_A, TYPE, CAT))
                .orElseThrow()
                .getTranCatBal();
        assertThat(catBal).isEqualByComparingTo("170.00"); // 0 + 200 - 30

        Integer rejects = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM posting_transaction_reject", Integer.class);
        assertThat(rejects).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT reject_reason_code FROM posting_transaction_reject WHERE dalytran_id = ?",
                Integer.class, "CS14PIPE00000003")).isEqualTo(100);
    }

    @Test
    void rejectsUnknownPipelineName() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> launcher.launch("does-not-exist"))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    private static Account account(String id, String creditLimit, String currBal,
                                   String cycCredit, String cycDebit) {
        Account a = new Account();
        a.setAcctId(id);
        a.setAcctActiveStatus("Y");
        a.setAcctCurrBal(new BigDecimal(currBal));
        a.setAcctCreditLimit(new BigDecimal(creditLimit));
        a.setAcctCashCreditLimit(new BigDecimal("0.00"));
        a.setAcctOpenDate("2000-01-01");
        a.setAcctExpirationDate("2099-12-31");
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

    private static DailyTransaction daily(String id, String cardNum, String amt, String origTs) {
        DailyTransaction t = new DailyTransaction();
        t.setDalytranId(id);
        t.setTranCardNum(cardNum);
        t.setTranTypeCd(TYPE);
        t.setTranCatCd(CAT);
        t.setTranSource("POS");
        t.setTranDesc("CS-14 pipeline test transaction");
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
