package com.carddemo.batch.posting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.DailyTransaction;
import com.carddemo.model.entity.Transaction;
import com.carddemo.model.entity.TransactionCategoryBalance;
import com.carddemo.model.entity.TransactionCategoryBalanceId;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * COBOL program: CBTRN02C — validation (1500-*) and posting (2000-*) rules, exercised against the
 * relational replacements of the CARDXREF, ACCTDATA, TCATBALF and TRANSACT files.
 */
@DataJpaTest
class TransactionPostingServiceTest {

    private static final String CARD = "4859452612877065";
    private static final long ACCOUNT_ID = 11L;
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2022-06-11T04:05:06.780Z"), ZoneId.of("UTC"));

    @Autowired
    private CardXrefRepository xrefs;
    @Autowired
    private AccountRepository accounts;
    @Autowired
    private TransactionCategoryBalanceRepository balances;
    @Autowired
    private TransactionRepository transactions;
    @Autowired
    private EntityManager entityManager;

    private TransactionPostingService service;

    @BeforeEach
    void setUp() {
        service = new TransactionPostingService(xrefs, accounts, balances, transactions, CLOCK);
        xrefs.save(CardXref.builder().cardNumber(CARD).customerId(9L).accountId(ACCOUNT_ID).build());
        accounts.save(account("2030-12-31", "2000.00", "100.00", "20.00", "500.00"));
        flush();
    }

    private void flush() {
        entityManager.flush();
        entityManager.clear();
    }

    private static Account account(String expiry, String creditLimit, String cycleCredit,
                                   String cycleDebit, String currentBalance) {
        return Account.builder()
                .accountId(ACCOUNT_ID)
                .activeStatus("Y")
                .currentBalance(new BigDecimal(currentBalance))
                .creditLimit(new BigDecimal(creditLimit))
                .cashCreditLimit(new BigDecimal("500.00"))
                .openDate("2014-11-20")
                .expirationDate(expiry)
                .reissueDate("2022-11-20")
                .currentCycleCredit(new BigDecimal(cycleCredit))
                .currentCycleDebit(new BigDecimal(cycleDebit))
                .addressZip("A000000000")
                .groupId("")
                .build();
    }

    private static DailyTransaction transaction(String amount) {
        return transaction(amount, CARD, "2022-06-10 19:27:53.000000");
    }

    private static DailyTransaction transaction(String amount, String cardNumber, String originTs) {
        return DailyTransaction.builder()
                .transactionId("0000000000683580")
                .typeCode("01")
                .categoryCode(1)
                .source("POS TERM")
                .description("Purchase at Abshire-Lowe")
                .amount(new BigDecimal(amount))
                .merchantId(800000000L)
                .merchantName("Abshire-Lowe")
                .merchantCity("North Enoshaven")
                .merchantZip("72112")
                .cardNumber(cardNumber)
                .originTimestamp(originTs)
                .processTimestamp("")
                .build();
    }

    private static TransactionCategoryBalanceId categoryKey() {
        return TransactionCategoryBalanceId.builder()
                .accountId(ACCOUNT_ID).typeCode("01").categoryCode(1).build();
    }

    @Test
    void postsAcceptedTransactionAndUpdatesBalances() {
        PostingResult result = service.post(transaction("504.77"));
        flush();

        assertThat(result.isPosted()).isTrue();
        assertThat(result.getRejectRecord()).isEmpty();
        assertThat(result.getAccountUpdateFailure()).isEmpty();

        Account account = accounts.findById(ACCOUNT_ID).orElseThrow();
        // 2800: ACCT-CURR-BAL 500.00 + 504.77, ACCT-CURR-CYC-CREDIT 100.00 + 504.77, debit untouched.
        assertThat(account.getCurrentBalance()).isEqualByComparingTo("1004.77");
        assertThat(account.getCurrentCycleCredit()).isEqualByComparingTo("604.77");
        assertThat(account.getCurrentCycleDebit()).isEqualByComparingTo("20.00");

        // 2700-A: the TCATBALF record did not exist, so it is created with the transaction amount.
        assertThat(balances.findById(categoryKey()).orElseThrow().getBalance())
                .isEqualByComparingTo("504.77");

        Transaction posted = transactions.findById("0000000000683580").orElseThrow();
        assertThat(posted.getAmount()).isEqualByComparingTo("504.77");
        assertThat(posted.getCardNumber()).isEqualTo(CARD);
        assertThat(posted.getOriginTimestamp()).isEqualTo("2022-06-10 19:27:53.000000");
        // Z-GET-DB2-FORMAT-TIMESTAMP: EEEE-MM-DD-UU.MM.SS.HH0000
        assertThat(posted.getProcessTimestamp()).isEqualTo("2022-06-11-04.05.06.780000");
    }

    @Test
    void addsToAnExistingCategoryBalance() {
        balances.save(TransactionCategoryBalance.builder()
                .id(categoryKey())
                .balance(new BigDecimal("100.23"))
                .build());
        flush();

        service.post(transaction("50.10"));
        flush();

        assertThat(balances.findById(categoryKey()).orElseThrow().getBalance())
                .isEqualByComparingTo("150.33");
    }

    @Test
    void negativeAmountUpdatesTheCycleDebitWithItsSign() {
        PostingResult result = service.post(transaction("-125.50"));
        flush();

        assertThat(result.isPosted()).isTrue();
        Account account = accounts.findById(ACCOUNT_ID).orElseThrow();
        assertThat(account.getCurrentBalance()).isEqualByComparingTo("374.50");
        assertThat(account.getCurrentCycleCredit()).isEqualByComparingTo("100.00");
        // ADD DALYTRAN-AMT TO ACCT-CURR-CYC-DEBIT with a negative amount: 20.00 + (-125.50).
        assertThat(account.getCurrentCycleDebit()).isEqualByComparingTo("-105.50");
        assertThat(balances.findById(categoryKey()).orElseThrow().getBalance())
                .isEqualByComparingTo("-125.50");
    }

    @Test
    void zeroAmountCountsAsACredit() {
        service.post(transaction("0.00"));
        flush();

        Account account = accounts.findById(ACCOUNT_ID).orElseThrow();
        assertThat(account.getCurrentCycleCredit()).isEqualByComparingTo("100.00");
        assertThat(account.getCurrentCycleDebit()).isEqualByComparingTo("20.00");
    }

    @Test
    void rejectsUnknownCardWithReason100() {
        PostingResult result = service.post(
                transaction("10.00", "9999999999999999", "2022-06-10 19:27:53.000000"));
        flush();

        TransactionRejectRecord reject = result.getRejectRecord().orElseThrow();
        assertThat(reject.getFailReason()).isEqualTo(100);
        assertThat(reject.getFailReasonDescription()).isEqualTo("INVALID CARD NUMBER FOUND");
        assertThat(transactions.count()).isZero();
        assertThat(balances.count()).isZero();
    }

    @Test
    void rejectsMissingAccountWithReason101() {
        accounts.deleteById(ACCOUNT_ID);
        flush();

        PostingResult result = service.post(transaction("10.00"));
        flush();

        TransactionRejectRecord reject = result.getRejectRecord().orElseThrow();
        assertThat(reject.getFailReason()).isEqualTo(101);
        assertThat(reject.getFailReasonDescription()).isEqualTo("ACCOUNT RECORD NOT FOUND");
        assertThat(transactions.count()).isZero();
    }

    @Test
    void rejectsOverlimitTransactionWithReason102() {
        // WS-TEMP-BAL = 100.00 - 20.00 + 1920.01 = 2000.01 > ACCT-CREDIT-LIMIT 2000.00
        PostingResult result = service.post(transaction("1920.01"));
        flush();

        TransactionRejectRecord reject = result.getRejectRecord().orElseThrow();
        assertThat(reject.getFailReason()).isEqualTo(102);
        assertThat(reject.getFailReasonDescription()).isEqualTo("OVERLIMIT TRANSACTION");
        assertThat(accounts.findById(ACCOUNT_ID).orElseThrow().getCurrentBalance())
                .isEqualByComparingTo("500.00");
        assertThat(transactions.count()).isZero();
    }

    @Test
    void acceptsTransactionExactlyOnTheCreditLimit() {
        // WS-TEMP-BAL = 100.00 - 20.00 + 1920.00 = 2000.00, ACCT-CREDIT-LIMIT >= WS-TEMP-BAL.
        PostingResult result = service.post(transaction("1920.00"));
        flush();

        assertThat(result.isPosted()).isTrue();
        assertThat(accounts.findById(ACCOUNT_ID).orElseThrow().getCurrentCycleCredit())
                .isEqualByComparingTo("2020.00");
    }

    @Test
    void rejectsTransactionAfterAccountExpirationWithReason103() {
        accounts.save(account("2022-06-09", "2000.00", "100.00", "20.00", "500.00"));
        flush();

        PostingResult result = service.post(transaction("10.00"));

        TransactionRejectRecord reject = result.getRejectRecord().orElseThrow();
        assertThat(reject.getFailReason()).isEqualTo(103);
        assertThat(reject.getFailReasonDescription())
                .isEqualTo("TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
    }

    @Test
    void acceptsTransactionOnTheExpirationDateItself() {
        accounts.save(account("2022-06-10", "2000.00", "100.00", "20.00", "500.00"));
        flush();

        assertThat(service.post(transaction("10.00")).isPosted()).isTrue();
    }

    @Test
    void expirationRejectOverridesOverlimitReject() {
        // Both checks fail; 1500-B-LOOKUP-ACCT evaluates the expiration check last, so 103 wins.
        accounts.save(account("2022-06-09", "2000.00", "100.00", "20.00", "500.00"));
        flush();

        assertThat(service.post(transaction("5000.00")).getRejectRecord().orElseThrow().getFailReason())
                .isEqualTo(103);
    }

    @Test
    void validateReportsTheReasonWithoutPosting() {
        assertThat(service.validate(transaction("1920.01")))
                .contains(PostingRejectReason.OVERLIMIT_TRANSACTION);
        assertThat(service.validate(transaction("1920.00"))).isEmpty();
        flush();

        assertThat(transactions.count()).isZero();
        assertThat(accounts.findById(ACCOUNT_ID).orElseThrow().getCurrentBalance())
                .isEqualByComparingTo("500.00");
    }

    @Test
    void reportsReason109WhenTheAccountRewriteFails() {
        // The COBOL REWRITE hits INVALID KEY: the account is gone by the time it is written back.
        AccountRepository failingAccounts = mock(AccountRepository.class);
        when(failingAccounts.findById(ACCOUNT_ID)).thenReturn(accounts.findById(ACCOUNT_ID));
        when(failingAccounts.existsById(any())).thenReturn(false);
        TransactionPostingService failing = new TransactionPostingService(
                xrefs, failingAccounts, balances, transactions, CLOCK);

        PostingResult result = failing.post(transaction("10.00"));
        flush();

        assertThat(result.isPosted()).isTrue();
        assertThat(result.getAccountUpdateFailure())
                .contains(PostingRejectReason.ACCOUNT_REWRITE_FAILED);
        assertThat(result.getAccountUpdateFailure().orElseThrow().getCode()).isEqualTo(109);
        // The transaction is still written to TRANSACT, as in 2000-POST-TRANSACTION.
        assertThat(transactions.count()).isEqualTo(1);
    }
}
