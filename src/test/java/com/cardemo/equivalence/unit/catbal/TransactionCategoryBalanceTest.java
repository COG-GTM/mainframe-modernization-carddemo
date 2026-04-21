package com.cardemo.equivalence.unit.catbal;

import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionCategoryBalanceId;
import com.cardemo.batch.repository.TransactionCategoryBalanceRepository;
import com.cardemo.batch.service.TransactionCategoryBalanceService;
import com.cardemo.batch.service.TransactionCategoryBalanceService.UpdateResult;
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
 * Tests for transaction category balance updates.
 * Business rule 10: Composite key (ACCT-ID, TYPE-CD, CAT-CD).
 * COBOL paragraph 2700-UPDATE-TCATBAL.
 * Create new if not found, update if found.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction Category Balance Tests")
class TransactionCategoryBalanceTest {

    @Mock
    private TransactionCategoryBalanceRepository repository;

    private TransactionCategoryBalanceService service;

    @BeforeEach
    void setUp() {
        service = new TransactionCategoryBalanceService(repository);
    }

    private DailyTransaction createTransaction(String typeCd, int catCd, BigDecimal amount) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("TXN_CAT");
        dt.setTypeCd(typeCd);
        dt.setCatCd(catCd);
        dt.setAmount(amount);
        return dt;
    }

    @Nested
    @DisplayName("Create New Balance (Not Found)")
    class CreateNew {

        @Test
        @DisplayName("Creates new record when composite key not found")
        void createNew_whenNotFound() {
            TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(1001L, "SA", 5);
            when(repository.findById(id)).thenReturn(Optional.empty());

            DailyTransaction txn = createTransaction("SA", 5, new BigDecimal("100.00"));
            UpdateResult result = service.updateCategoryBalance(1001L, txn);

            assertTrue(result.created());
        }

        @Test
        @DisplayName("New record has correct composite key")
        void createNew_correctCompositeKey() {
            TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(2001L, "PR", 3);
            when(repository.findById(id)).thenReturn(Optional.empty());

            DailyTransaction txn = createTransaction("PR", 3, new BigDecimal("50.00"));
            UpdateResult result = service.updateCategoryBalance(2001L, txn);

            TransactionCategoryBalance catBal = result.categoryBalance();
            assertEquals(2001L, catBal.getAcctId());
            assertEquals("PR", catBal.getTypeCd());
            assertEquals(3, catBal.getCatCd());
        }

        @Test
        @DisplayName("New record has transaction amount as initial balance")
        void createNew_initialBalance() {
            TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(3001L, "SA", 1);
            when(repository.findById(id)).thenReturn(Optional.empty());

            DailyTransaction txn = createTransaction("SA", 1, new BigDecimal("250.00"));
            UpdateResult result = service.updateCategoryBalance(3001L, txn);

            assertEquals(new BigDecimal("250.00"), result.categoryBalance().getBalance());
        }

        @Test
        @DisplayName("Creates new with negative amount")
        void createNew_negativeAmount() {
            TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(4001L, "SA", 1);
            when(repository.findById(id)).thenReturn(Optional.empty());

            DailyTransaction txn = createTransaction("SA", 1, new BigDecimal("-100.00"));
            UpdateResult result = service.updateCategoryBalance(4001L, txn);

            assertEquals(new BigDecimal("-100.00"), result.categoryBalance().getBalance());
        }
    }

    @Nested
    @DisplayName("Update Existing Balance (Found)")
    class UpdateExisting {

        @Test
        @DisplayName("Updates existing record when composite key found")
        void updateExisting_whenFound() {
            TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(1001L, "SA", 5);
            TransactionCategoryBalance existing = new TransactionCategoryBalance(
                    1001L, "SA", 5, new BigDecimal("500.00"));
            when(repository.findById(id)).thenReturn(Optional.of(existing));

            DailyTransaction txn = createTransaction("SA", 5, new BigDecimal("100.00"));
            UpdateResult result = service.updateCategoryBalance(1001L, txn);

            assertFalse(result.created());
        }

        @Test
        @DisplayName("Adds transaction amount to existing balance")
        void updateExisting_addsToBalance() {
            TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(2001L, "PR", 3);
            TransactionCategoryBalance existing = new TransactionCategoryBalance(
                    2001L, "PR", 3, new BigDecimal("1000.00"));
            when(repository.findById(id)).thenReturn(Optional.of(existing));

            DailyTransaction txn = createTransaction("PR", 3, new BigDecimal("250.00"));
            UpdateResult result = service.updateCategoryBalance(2001L, txn);

            assertEquals(new BigDecimal("1250.00"), result.categoryBalance().getBalance());
        }

        @Test
        @DisplayName("Subtracts when negative amount")
        void updateExisting_subtractsNegative() {
            TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(3001L, "SA", 1);
            TransactionCategoryBalance existing = new TransactionCategoryBalance(
                    3001L, "SA", 1, new BigDecimal("500.00"));
            when(repository.findById(id)).thenReturn(Optional.of(existing));

            DailyTransaction txn = createTransaction("SA", 1, new BigDecimal("-200.00"));
            UpdateResult result = service.updateCategoryBalance(3001L, txn);

            assertEquals(new BigDecimal("300.00"), result.categoryBalance().getBalance());
        }

        @Test
        @DisplayName("Balance can go negative after update")
        void updateExisting_balanceGoesNegative() {
            TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(4001L, "SA", 1);
            TransactionCategoryBalance existing = new TransactionCategoryBalance(
                    4001L, "SA", 1, new BigDecimal("100.00"));
            when(repository.findById(id)).thenReturn(Optional.of(existing));

            DailyTransaction txn = createTransaction("SA", 1, new BigDecimal("-500.00"));
            UpdateResult result = service.updateCategoryBalance(4001L, txn);

            assertEquals(new BigDecimal("-400.00"), result.categoryBalance().getBalance());
        }

        @Test
        @DisplayName("Zero amount does not change balance")
        void updateExisting_zeroAmount() {
            TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(5001L, "SA", 1);
            TransactionCategoryBalance existing = new TransactionCategoryBalance(
                    5001L, "SA", 1, new BigDecimal("750.00"));
            when(repository.findById(id)).thenReturn(Optional.of(existing));

            DailyTransaction txn = createTransaction("SA", 1, BigDecimal.ZERO);
            UpdateResult result = service.updateCategoryBalance(5001L, txn);

            assertEquals(new BigDecimal("750.00"), result.categoryBalance().getBalance());
        }
    }

    @Nested
    @DisplayName("Composite Key Semantics")
    class CompositeKey {

        @Test
        @DisplayName("Different type codes are separate records")
        void differentTypeCodes_separateRecords() {
            TransactionCategoryBalanceId id1 = new TransactionCategoryBalanceId(1L, "SA", 1);
            TransactionCategoryBalanceId id2 = new TransactionCategoryBalanceId(1L, "PR", 1);

            when(repository.findById(id1)).thenReturn(Optional.empty());
            when(repository.findById(id2)).thenReturn(Optional.empty());

            UpdateResult r1 = service.updateCategoryBalance(1L, createTransaction("SA", 1, new BigDecimal("100.00")));
            UpdateResult r2 = service.updateCategoryBalance(1L, createTransaction("PR", 1, new BigDecimal("200.00")));

            assertTrue(r1.created());
            assertTrue(r2.created());
            assertEquals(new BigDecimal("100.00"), r1.categoryBalance().getBalance());
            assertEquals(new BigDecimal("200.00"), r2.categoryBalance().getBalance());
        }

        @Test
        @DisplayName("Different category codes are separate records")
        void differentCatCodes_separateRecords() {
            TransactionCategoryBalanceId id1 = new TransactionCategoryBalanceId(1L, "SA", 1);
            TransactionCategoryBalanceId id2 = new TransactionCategoryBalanceId(1L, "SA", 2);

            when(repository.findById(id1)).thenReturn(Optional.empty());
            when(repository.findById(id2)).thenReturn(Optional.empty());

            UpdateResult r1 = service.updateCategoryBalance(1L, createTransaction("SA", 1, new BigDecimal("100.00")));
            UpdateResult r2 = service.updateCategoryBalance(1L, createTransaction("SA", 2, new BigDecimal("200.00")));

            assertTrue(r1.created());
            assertTrue(r2.created());
            assertNotEquals(r1.categoryBalance().getCatCd(), r2.categoryBalance().getCatCd());
        }

        @Test
        @DisplayName("Different account IDs are separate records")
        void differentAcctIds_separateRecords() {
            TransactionCategoryBalanceId id1 = new TransactionCategoryBalanceId(1L, "SA", 1);
            TransactionCategoryBalanceId id2 = new TransactionCategoryBalanceId(2L, "SA", 1);

            when(repository.findById(id1)).thenReturn(Optional.empty());
            when(repository.findById(id2)).thenReturn(Optional.empty());

            UpdateResult r1 = service.updateCategoryBalance(1L, createTransaction("SA", 1, new BigDecimal("100.00")));
            UpdateResult r2 = service.updateCategoryBalance(2L, createTransaction("SA", 1, new BigDecimal("200.00")));

            assertTrue(r1.created());
            assertTrue(r2.created());
            assertNotEquals(r1.categoryBalance().getAcctId(), r2.categoryBalance().getAcctId());
        }

        @Test
        @DisplayName("Composite key equality check")
        void compositeKeyEquality() {
            TransactionCategoryBalanceId id1 = new TransactionCategoryBalanceId(100L, "SA", 5);
            TransactionCategoryBalanceId id2 = new TransactionCategoryBalanceId(100L, "SA", 5);
            assertEquals(id1, id2);
        }

        @Test
        @DisplayName("Composite key inequality with different fields")
        void compositeKeyInequality() {
            TransactionCategoryBalanceId id1 = new TransactionCategoryBalanceId(100L, "SA", 5);
            TransactionCategoryBalanceId id2 = new TransactionCategoryBalanceId(100L, "SA", 6);
            assertNotEquals(id1, id2);
        }
    }
}
