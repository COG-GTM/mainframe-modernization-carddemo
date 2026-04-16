package com.cardemo.equivalence.unit.creditdebit;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.service.AccountUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for credit/debit separation per COBOL paragraph 2800-UPDATE-ACCOUNT-REC.
 * Business rule 8:
 * - AMT >= 0 → ACCT-CURR-CYC-CREDIT
 * - AMT < 0  → ACCT-CURR-CYC-DEBIT
 * - Always: ADD AMT TO ACCT-CURR-BAL
 */
@DisplayName("Credit/Debit Separation Tests")
class CreditDebitSeparationTest {

    private AccountUpdateService accountUpdateService;

    @BeforeEach
    void setUp() {
        accountUpdateService = new AccountUpdateService();
    }

    private Account createAccount(BigDecimal balance, BigDecimal credit, BigDecimal debit) {
        Account account = new Account();
        account.setAcctId(1L);
        account.setCurrentBalance(balance);
        account.setCurrentCycleCredit(credit);
        account.setCurrentCycleDebit(debit);
        return account;
    }

    private DailyTransaction createTransaction(BigDecimal amount) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("TXN_CD");
        dt.setAmount(amount);
        return dt;
    }

    @Nested
    @DisplayName("Positive Amount (Credit)")
    class PositiveAmount {

        @Test
        @DisplayName("Positive amount adds to ACCT-CURR-CYC-CREDIT")
        void positiveAmount_addsToCycleCredit() {
            Account account = createAccount(new BigDecimal("1000.00"),
                    new BigDecimal("200.00"), BigDecimal.ZERO);
            DailyTransaction txn = createTransaction(new BigDecimal("100.00"));

            accountUpdateService.updateAccountBalances(account, txn);

            assertEquals(new BigDecimal("300.00"), account.getCurrentCycleCredit());
        }

        @Test
        @DisplayName("Positive amount does not affect ACCT-CURR-CYC-DEBIT")
        void positiveAmount_doesNotAffectDebit() {
            Account account = createAccount(new BigDecimal("1000.00"),
                    BigDecimal.ZERO, new BigDecimal("50.00"));
            DailyTransaction txn = createTransaction(new BigDecimal("100.00"));

            accountUpdateService.updateAccountBalances(account, txn);

            assertEquals(new BigDecimal("50.00"), account.getCurrentCycleDebit());
        }

        @Test
        @DisplayName("Positive amount adds to current balance")
        void positiveAmount_addsToBalance() {
            Account account = createAccount(new BigDecimal("1000.00"),
                    BigDecimal.ZERO, BigDecimal.ZERO);
            DailyTransaction txn = createTransaction(new BigDecimal("250.00"));

            accountUpdateService.updateAccountBalances(account, txn);

            assertEquals(new BigDecimal("1250.00"), account.getCurrentBalance());
        }

        @Test
        @DisplayName("Large positive amount")
        void largePositiveAmount() {
            Account account = createAccount(new BigDecimal("50000.00"),
                    new BigDecimal("10000.00"), BigDecimal.ZERO);
            DailyTransaction txn = createTransaction(new BigDecimal("25000.00"));

            accountUpdateService.updateAccountBalances(account, txn);

            assertEquals(new BigDecimal("35000.00"), account.getCurrentCycleCredit());
            assertEquals(new BigDecimal("75000.00"), account.getCurrentBalance());
        }

        @Test
        @DisplayName("One cent positive amount")
        void oneCentPositive() {
            Account account = createAccount(new BigDecimal("100.00"),
                    BigDecimal.ZERO, BigDecimal.ZERO);
            DailyTransaction txn = createTransaction(new BigDecimal("0.01"));

            accountUpdateService.updateAccountBalances(account, txn);

            assertEquals(new BigDecimal("0.01"), account.getCurrentCycleCredit());
            assertEquals(new BigDecimal("100.01"), account.getCurrentBalance());
        }
    }

    @Nested
    @DisplayName("Negative Amount (Debit)")
    class NegativeAmount {

        @Test
        @DisplayName("Negative amount adds to ACCT-CURR-CYC-DEBIT")
        void negativeAmount_addsToCycleDebit() {
            Account account = createAccount(new BigDecimal("1000.00"),
                    BigDecimal.ZERO, new BigDecimal("100.00"));
            DailyTransaction txn = createTransaction(new BigDecimal("-75.00"));

            accountUpdateService.updateAccountBalances(account, txn);

            assertEquals(new BigDecimal("25.00"), account.getCurrentCycleDebit());
        }

        @Test
        @DisplayName("Negative amount does not affect ACCT-CURR-CYC-CREDIT")
        void negativeAmount_doesNotAffectCredit() {
            Account account = createAccount(new BigDecimal("1000.00"),
                    new BigDecimal("500.00"), BigDecimal.ZERO);
            DailyTransaction txn = createTransaction(new BigDecimal("-200.00"));

            accountUpdateService.updateAccountBalances(account, txn);

            assertEquals(new BigDecimal("500.00"), account.getCurrentCycleCredit());
        }

        @Test
        @DisplayName("Negative amount reduces current balance")
        void negativeAmount_reducesBalance() {
            Account account = createAccount(new BigDecimal("1000.00"),
                    BigDecimal.ZERO, BigDecimal.ZERO);
            DailyTransaction txn = createTransaction(new BigDecimal("-300.00"));

            accountUpdateService.updateAccountBalances(account, txn);

            assertEquals(new BigDecimal("700.00"), account.getCurrentBalance());
        }

        @Test
        @DisplayName("Negative amount can make balance negative")
        void negativeAmount_balanceGoesNegative() {
            Account account = createAccount(new BigDecimal("100.00"),
                    BigDecimal.ZERO, BigDecimal.ZERO);
            DailyTransaction txn = createTransaction(new BigDecimal("-500.00"));

            accountUpdateService.updateAccountBalances(account, txn);

            assertEquals(new BigDecimal("-400.00"), account.getCurrentBalance());
        }
    }

    @Nested
    @DisplayName("Zero Amount")
    class ZeroAmount {

        @Test
        @DisplayName("Zero amount goes to credit path (AMT >= 0)")
        void zeroAmount_goesToCredit() {
            Account account = createAccount(new BigDecimal("1000.00"),
                    new BigDecimal("100.00"), new BigDecimal("50.00"));
            DailyTransaction txn = createTransaction(BigDecimal.ZERO);

            accountUpdateService.updateAccountBalances(account, txn);

            assertEquals(new BigDecimal("100.00"), account.getCurrentCycleCredit());
            assertEquals(new BigDecimal("50.00"), account.getCurrentCycleDebit());
        }

        @Test
        @DisplayName("Zero amount does not change balance")
        void zeroAmount_balanceUnchanged() {
            Account account = createAccount(new BigDecimal("1000.00"),
                    BigDecimal.ZERO, BigDecimal.ZERO);
            DailyTransaction txn = createTransaction(BigDecimal.ZERO);

            accountUpdateService.updateAccountBalances(account, txn);

            assertEquals(new BigDecimal("1000.00"), account.getCurrentBalance());
        }
    }

    @Nested
    @DisplayName("Always Updates Balance")
    class AlwaysUpdatesBalance {

        @Test
        @DisplayName("Balance always updated regardless of sign")
        void balanceAlwaysUpdated() {
            Account account1 = createAccount(new BigDecimal("500.00"),
                    BigDecimal.ZERO, BigDecimal.ZERO);
            accountUpdateService.updateAccountBalances(account1, createTransaction(new BigDecimal("100.00")));
            assertEquals(new BigDecimal("600.00"), account1.getCurrentBalance());

            Account account2 = createAccount(new BigDecimal("500.00"),
                    BigDecimal.ZERO, BigDecimal.ZERO);
            accountUpdateService.updateAccountBalances(account2, createTransaction(new BigDecimal("-100.00")));
            assertEquals(new BigDecimal("400.00"), account2.getCurrentBalance());

            Account account3 = createAccount(new BigDecimal("500.00"),
                    BigDecimal.ZERO, BigDecimal.ZERO);
            accountUpdateService.updateAccountBalances(account3, createTransaction(BigDecimal.ZERO));
            assertEquals(new BigDecimal("500.00"), account3.getCurrentBalance());
        }

        @Test
        @DisplayName("Multiple sequential updates accumulate correctly")
        void multipleUpdates_accumulate() {
            Account account = createAccount(new BigDecimal("1000.00"),
                    BigDecimal.ZERO, BigDecimal.ZERO);

            accountUpdateService.updateAccountBalances(account, createTransaction(new BigDecimal("200.00")));
            accountUpdateService.updateAccountBalances(account, createTransaction(new BigDecimal("-50.00")));
            accountUpdateService.updateAccountBalances(account, createTransaction(new BigDecimal("100.00")));

            assertEquals(new BigDecimal("1250.00"), account.getCurrentBalance());
            assertEquals(new BigDecimal("300.00"), account.getCurrentCycleCredit());
            assertEquals(new BigDecimal("-50.00"), account.getCurrentCycleDebit());
        }
    }
}
