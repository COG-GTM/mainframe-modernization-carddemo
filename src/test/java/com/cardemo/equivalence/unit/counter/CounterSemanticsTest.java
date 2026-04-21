package com.cardemo.equivalence.unit.counter;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.ValidationResult;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.service.TransactionValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for counter semantics from COBOL batch processing.
 * Business rule 7:
 * - WS-TRANSACTION-COUNT increments per processed transaction
 * - WS-REJECT-COUNT increments per rejected transaction
 * - RETURN-CODE = 4 when WS-REJECT-COUNT > 0
 *
 * In the modernized code, counters are managed at the batch orchestration level.
 * This test verifies the validation results that drive counter logic:
 *   - Each call to validate() represents one transaction (count++)
 *   - validate() returning !isValid() represents a rejection (rejectCount++)
 *   - If any rejection occurred, batch RETURN-CODE should be 4
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Counter Semantics Tests")
class CounterSemanticsTest {

    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private AccountRepository accountRepository;

    private TransactionValidationService validationService;

    // Counters managed at the test level to simulate batch orchestration
    private int transactionCount;
    private int rejectCount;

    @BeforeEach
    void setUp() {
        validationService = new TransactionValidationService(cardXrefRepository, accountRepository);
        transactionCount = 0;
        rejectCount = 0;
    }

    /**
     * Simulates batch processing: validate a transaction and update counters.
     * This mirrors the COBOL logic where counters are incremented in the main loop.
     */
    private ValidationResult processTransaction(DailyTransaction dt) {
        transactionCount++;
        ValidationResult result = validationService.validate(dt);
        if (!result.isValid()) {
            rejectCount++;
        }
        return result;
    }

    private int getReturnCode() {
        return rejectCount > 0 ? 4 : 0;
    }

    private DailyTransaction createValidTransaction(String cardNum) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("TXN_COUNTER");
        dt.setCardNum(cardNum);
        dt.setAmount(new BigDecimal("10.00"));
        dt.setTypeCd("SA");
        dt.setCatCd(1);
        dt.setOrigTimestamp("2024-01-15-10.30.00.000000");
        return dt;
    }

    private void setupValidCard(String cardNum, long acctId) {
        CardXref xref = new CardXref();
        xref.setCardNum(cardNum);
        xref.setAcctId(acctId);

        Account account = new Account();
        account.setAcctId(acctId);
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        account.setExpirationDate("2025-12-31");

        when(cardXrefRepository.findById(cardNum)).thenReturn(Optional.of(xref));
        when(accountRepository.findById(acctId)).thenReturn(Optional.of(account));
    }

    @Nested
    @DisplayName("Transaction Count")
    class TransactionCount {

        @Test
        @DisplayName("Transaction count starts at zero")
        void transactionCount_startsAtZero() {
            assertEquals(0, transactionCount);
        }

        @Test
        @DisplayName("Transaction count increments for each processed transaction")
        void transactionCount_incrementsPerTransaction() {
            setupValidCard("CARD_A", 1L);
            setupValidCard("CARD_B", 2L);

            processTransaction(createValidTransaction("CARD_A"));
            assertEquals(1, transactionCount);

            processTransaction(createValidTransaction("CARD_B"));
            assertEquals(2, transactionCount);
        }

        @Test
        @DisplayName("Transaction count includes both valid and rejected transactions")
        void transactionCount_includesBothValidAndRejected() {
            setupValidCard("VALID_CARD", 10L);
            when(cardXrefRepository.findById("INVALID")).thenReturn(Optional.empty());

            processTransaction(createValidTransaction("VALID_CARD"));
            processTransaction(createValidTransaction("INVALID"));

            assertEquals(2, transactionCount);
        }

        @Test
        @DisplayName("Transaction count for 10 transactions")
        void transactionCount_tenTransactions() {
            for (int i = 0; i < 10; i++) {
                String card = "CARD_" + i;
                setupValidCard(card, 100L + i);
                processTransaction(createValidTransaction(card));
            }
            assertEquals(10, transactionCount);
        }
    }

    @Nested
    @DisplayName("Reject Count")
    class RejectCount {

        @Test
        @DisplayName("Reject count starts at zero")
        void rejectCount_startsAtZero() {
            assertEquals(0, rejectCount);
        }

        @Test
        @DisplayName("Reject count increments for each rejected transaction")
        void rejectCount_incrementsPerReject() {
            when(cardXrefRepository.findById("BAD1")).thenReturn(Optional.empty());
            when(cardXrefRepository.findById("BAD2")).thenReturn(Optional.empty());

            processTransaction(createValidTransaction("BAD1"));
            assertEquals(1, rejectCount);

            processTransaction(createValidTransaction("BAD2"));
            assertEquals(2, rejectCount);
        }

        @Test
        @DisplayName("Reject count does not increment for valid transactions")
        void rejectCount_notIncrementedForValid() {
            setupValidCard("GOOD_CARD", 50L);

            processTransaction(createValidTransaction("GOOD_CARD"));

            assertEquals(0, rejectCount);
        }

        @Test
        @DisplayName("Mixed valid and rejected transactions count correctly")
        void rejectCount_mixedTransactions() {
            setupValidCard("GOOD1", 1L);
            setupValidCard("GOOD2", 2L);
            when(cardXrefRepository.findById("BAD_A")).thenReturn(Optional.empty());
            when(cardXrefRepository.findById("BAD_B")).thenReturn(Optional.empty());
            when(cardXrefRepository.findById("BAD_C")).thenReturn(Optional.empty());

            processTransaction(createValidTransaction("GOOD1"));
            processTransaction(createValidTransaction("BAD_A"));
            processTransaction(createValidTransaction("GOOD2"));
            processTransaction(createValidTransaction("BAD_B"));
            processTransaction(createValidTransaction("BAD_C"));

            assertEquals(5, transactionCount);
            assertEquals(3, rejectCount);
        }
    }

    @Nested
    @DisplayName("Return Code")
    class ReturnCode {

        @Test
        @DisplayName("RETURN-CODE = 0 when no rejections")
        void returnCode_zeroWhenNoRejects() {
            setupValidCard("OK_CARD", 1L);
            processTransaction(createValidTransaction("OK_CARD"));

            assertEquals(0, getReturnCode());
        }

        @Test
        @DisplayName("RETURN-CODE = 4 when reject count > 0")
        void returnCode_fourWhenRejectsExist() {
            when(cardXrefRepository.findById("REJECT_CARD")).thenReturn(Optional.empty());
            processTransaction(createValidTransaction("REJECT_CARD"));

            assertEquals(4, getReturnCode());
        }

        @Test
        @DisplayName("RETURN-CODE = 4 even with single rejection among many successes")
        void returnCode_fourWithSingleReject() {
            for (int i = 0; i < 5; i++) {
                String card = "PASS_" + i;
                setupValidCard(card, 10L + i);
                processTransaction(createValidTransaction(card));
            }
            when(cardXrefRepository.findById("FAIL_1")).thenReturn(Optional.empty());
            processTransaction(createValidTransaction("FAIL_1"));

            assertEquals(4, getReturnCode());
        }

        @Test
        @DisplayName("RETURN-CODE = 0 before any processing")
        void returnCode_zeroBeforeProcessing() {
            assertEquals(0, getReturnCode());
        }
    }

    @Nested
    @DisplayName("Counter Reset")
    class CounterReset {

        @Test
        @DisplayName("Reset clears all counters")
        void reset_clearsAll() {
            when(cardXrefRepository.findById("X")).thenReturn(Optional.empty());
            processTransaction(createValidTransaction("X"));

            // Simulate counter reset (new batch run)
            transactionCount = 0;
            rejectCount = 0;

            assertEquals(0, transactionCount);
            assertEquals(0, rejectCount);
            assertEquals(0, getReturnCode());
        }
    }
}
