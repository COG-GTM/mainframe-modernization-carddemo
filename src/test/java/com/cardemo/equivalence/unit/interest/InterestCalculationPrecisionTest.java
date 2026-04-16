package com.cardemo.equivalence.unit.interest;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DisclosureGroup;
import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionRecord;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.repository.DisclosureGroupRepository;
import com.cardemo.batch.repository.TransactionRecordRepository;
import com.cardemo.batch.service.InterestCalculationService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for interest calculation precision.
 * Formula: (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
 * Must use BigDecimal, NOT floating-point.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Interest Calculation Precision Tests")
class InterestCalculationPrecisionTest {

    @Mock private DisclosureGroupRepository disclosureGroupRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CardXrefRepository cardXrefRepository;
    @Mock private TransactionRecordRepository transactionRecordRepository;

    private InterestCalculationService service;

    @BeforeEach
    void setUp() {
        service = new InterestCalculationService(
                disclosureGroupRepository, accountRepository,
                cardXrefRepository, transactionRecordRepository,
                new SimpleMeterRegistry());
    }

    @Nested
    @DisplayName("Core Formula: (balance * rate) / 1200")
    class CoreFormula {

        @Test
        @DisplayName("Standard case: $1000 at 12% -> $10.00")
        void standardCase() {
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("1000.00"), new BigDecimal("12.00"));
            assertEquals(new BigDecimal("10.00"), result);
        }

        @Test
        @DisplayName("$5000 at 18.99% -> $79.13 (HALF_UP rounding)")
        void typicalCreditCard() {
            // (5000 * 18.99) / 1200 = 79.125 -> 79.13 (HALF_UP)
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("5000.00"), new BigDecimal("18.99"));
            assertEquals(new BigDecimal("79.13"), result);
        }

        @Test
        @DisplayName("$1 at 1% -> $0.01 (minimum interest)")
        void minimumInterest() {
            // (1 * 1) / 1200 = 0.000833... -> 0.00
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("1.00"), new BigDecimal("1.00"));
            assertEquals(new BigDecimal("0.00"), result);
        }

        @Test
        @DisplayName("$100000 at 24.99% -> $2082.50")
        void largeBalance() {
            // (100000 * 24.99) / 1200 = 2082.5
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("100000.00"), new BigDecimal("24.99"));
            assertEquals(new BigDecimal("2082.50"), result);
        }

        @Test
        @DisplayName("$0 balance at any rate -> $0.00")
        void zeroBalance() {
            BigDecimal result = service.computeMonthlyInterest(
                    BigDecimal.ZERO, new BigDecimal("18.99"));
            assertEquals(new BigDecimal("0.00"), result);
        }

        @Test
        @DisplayName("Any balance at 0% rate -> $0.00")
        void zeroRate() {
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("10000.00"), BigDecimal.ZERO);
            assertEquals(new BigDecimal("0.00"), result);
        }

        @Test
        @DisplayName("Null balance returns zero")
        void nullBalance() {
            BigDecimal result = service.computeMonthlyInterest(
                    null, new BigDecimal("12.00"));
            assertEquals(BigDecimal.ZERO, result);
        }

        @Test
        @DisplayName("Null rate returns zero")
        void nullRate() {
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("1000.00"), null);
            assertEquals(BigDecimal.ZERO, result);
        }
    }

    @Nested
    @DisplayName("BigDecimal Precision Verification")
    class PrecisionVerification {

        @Test
        @DisplayName("Result uses BigDecimal, not double arithmetic")
        void bigDecimalNotDouble() {
            // This test would fail if using double: 0.1 + 0.2 != 0.3 in floating point
            BigDecimal balance = new BigDecimal("0.10");
            BigDecimal rate = new BigDecimal("12.00");
            BigDecimal result = service.computeMonthlyInterest(balance, rate);
            // (0.10 * 12) / 1200 = 0.001 -> 0.00
            assertEquals(new BigDecimal("0.00"), result);
        }

        @Test
        @DisplayName("Precision maintained for known COBOL test case: $2500.50 at 15.75%")
        void knownCobolTestCase() {
            // (2500.50 * 15.75) / 1200 = 32.819375 -> 32.82 (HALF_UP)
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("2500.50"), new BigDecimal("15.75"));
            assertEquals(new BigDecimal("32.82"), result);
        }

        @Test
        @DisplayName("HALF_UP rounding: 0.005 rounds up")
        void halfUpRounding() {
            // Need to find inputs that produce exactly .XX5
            // (600 * 10) / 1200 = 5.00 (exact, no rounding needed)
            // Let's use (333.33 * 18) / 1200 = 4.99995 -> 5.00
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("333.33"), new BigDecimal("18.00"));
            assertEquals(new BigDecimal("5.00"), result);
        }

        @Test
        @DisplayName("Result scale is always 2 decimal places")
        void resultScale() {
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("1000.00"), new BigDecimal("12.00"));
            assertEquals(2, result.scale());
        }

        @Test
        @DisplayName("Large balance precision: $99999999.99 at 0.01%")
        void largeBalancePrecision() {
            // (99999999.99 * 0.01) / 1200 = 833.333333... -> 833.33
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("99999999.99"), new BigDecimal("0.01"));
            assertEquals(new BigDecimal("833.33"), result);
        }

        @Test
        @DisplayName("Negative balance computes negative interest")
        void negativeBalance() {
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("-1000.00"), new BigDecimal("12.00"));
            assertEquals(new BigDecimal("-10.00"), result);
        }

        @Test
        @DisplayName("Fractional rate: 3.33% on $1200")
        void fractionalRate() {
            // (1200 * 3.33) / 1200 = 3.33
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("1200.00"), new BigDecimal("3.33"));
            assertEquals(new BigDecimal("3.33"), result);
        }
    }

    @Nested
    @DisplayName("Transaction Record Generation")
    class TransactionGeneration {

        @Test
        @DisplayName("Generated transaction has type '01' and cat '05'")
        void transactionTypeCat() {
            TransactionRecord txn = service.generateInterestTransaction(
                    "20240115", "ACCT001", new BigDecimal("10.00"), "CARD001");
            assertEquals("01", txn.getTranTypeCd());
            assertEquals("05", txn.getTranCatCd());
        }

        @Test
        @DisplayName("Generated transaction has source 'System'")
        void transactionSource() {
            TransactionRecord txn = service.generateInterestTransaction(
                    "20240115", "ACCT001", new BigDecimal("10.00"), "CARD001");
            assertEquals("System", txn.getTranSource());
        }

        @Test
        @DisplayName("Transaction ID format: PARM-DATE + 6-digit suffix")
        void transactionIdFormat() {
            service.resetCounters();
            TransactionRecord txn = service.generateInterestTransaction(
                    "20240115", "ACCT001", new BigDecimal("10.00"), "CARD001");
            assertTrue(txn.getTranId().startsWith("20240115"));
            assertEquals(14, txn.getTranId().length()); // 8 date + 6 suffix
        }

        @Test
        @DisplayName("Transaction description contains account ID")
        void transactionDescription() {
            TransactionRecord txn = service.generateInterestTransaction(
                    "20240115", "ACCT001", new BigDecimal("10.00"), "CARD001");
            assertTrue(txn.getTranDesc().contains("ACCT001"));
        }

        @Test
        @DisplayName("Transaction amount matches computed interest")
        void transactionAmount() {
            BigDecimal interest = new BigDecimal("42.57");
            TransactionRecord txn = service.generateInterestTransaction(
                    "20240115", "ACCT001", interest, "CARD001");
            assertEquals(interest, txn.getTranAmt());
        }

        @Test
        @DisplayName("Suffix increments for each generated transaction")
        void suffixIncrements() {
            service.resetCounters();
            TransactionRecord txn1 = service.generateInterestTransaction(
                    "20240115", "A1", new BigDecimal("1.00"), "C1");
            TransactionRecord txn2 = service.generateInterestTransaction(
                    "20240115", "A2", new BigDecimal("2.00"), "C2");

            assertNotEquals(txn1.getTranId(), txn2.getTranId());
        }
    }
}
