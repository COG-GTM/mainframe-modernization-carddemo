package com.cardemo.batch.service;

import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionCategoryBalanceId;
import com.cardemo.batch.repository.TransactionCategoryBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionCategoryBalanceService.
 * Tests paragraph 2700-UPDATE-TCATBAL logic from CBTRN02C.
 */
@ExtendWith(MockitoExtension.class)
class TransactionCategoryBalanceServiceTest {

    @Mock
    private TransactionCategoryBalanceRepository repository;

    private TransactionCategoryBalanceService service;

    @BeforeEach
    void setUp() {
        service = new TransactionCategoryBalanceService(repository);
    }

    private DailyTransaction createDailyTransaction(String typeCd, int catCd, BigDecimal amount) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("TRAN0000000001");
        dt.setTypeCd(typeCd);
        dt.setCatCd(catCd);
        dt.setAmount(amount);
        return dt;
    }

    @Test
    @DisplayName("2700-A: Create new TCATBAL record when not found (status '23')")
    void updateCategoryBalance_notFound_createsNew() {
        long acctId = 12345678901L;
        DailyTransaction dt = createDailyTransaction("SA", 5001, new BigDecimal("100.00"));
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(acctId, "SA", 5001);
        when(repository.findById(id)).thenReturn(Optional.empty());

        TransactionCategoryBalanceService.UpdateResult result = service.updateCategoryBalance(acctId, dt);

        assertTrue(result.created());
        assertEquals(acctId, result.categoryBalance().getAcctId());
        assertEquals("SA", result.categoryBalance().getTypeCd());
        assertEquals(5001, result.categoryBalance().getCatCd());
        assertEquals(new BigDecimal("100.00"), result.categoryBalance().getBalance());
    }

    @Test
    @DisplayName("2700-B: Update existing TCATBAL record by adding amount")
    void updateCategoryBalance_found_updatesExisting() {
        long acctId = 12345678901L;
        DailyTransaction dt = createDailyTransaction("SA", 5001, new BigDecimal("50.00"));
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(acctId, "SA", 5001);

        TransactionCategoryBalance existing = new TransactionCategoryBalance();
        existing.setAcctId(acctId);
        existing.setTypeCd("SA");
        existing.setCatCd(5001);
        existing.setBalance(new BigDecimal("200.00"));
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        TransactionCategoryBalanceService.UpdateResult result = service.updateCategoryBalance(acctId, dt);

        assertFalse(result.created());
        assertEquals(new BigDecimal("250.00"), result.categoryBalance().getBalance());
    }

    @Test
    @DisplayName("2700-A: New record initializes balance to zero before adding amount")
    void updateCategoryBalance_newRecord_initializesToZeroThenAdds() {
        long acctId = 99999999999L;
        DailyTransaction dt = createDailyTransaction("PR", 3002, new BigDecimal("-25.50"));
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(acctId, "PR", 3002);
        when(repository.findById(id)).thenReturn(Optional.empty());

        TransactionCategoryBalanceService.UpdateResult result = service.updateCategoryBalance(acctId, dt);

        assertTrue(result.created());
        assertEquals(new BigDecimal("-25.50"), result.categoryBalance().getBalance());
    }

    @Test
    @DisplayName("2700-B: Negative amount correctly subtracted from existing balance")
    void updateCategoryBalance_negativeAmount_subtractsFromBalance() {
        long acctId = 12345678901L;
        DailyTransaction dt = createDailyTransaction("SA", 5001, new BigDecimal("-30.00"));
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(acctId, "SA", 5001);

        TransactionCategoryBalance existing = new TransactionCategoryBalance();
        existing.setAcctId(acctId);
        existing.setTypeCd("SA");
        existing.setCatCd(5001);
        existing.setBalance(new BigDecimal("100.00"));
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        TransactionCategoryBalanceService.UpdateResult result = service.updateCategoryBalance(acctId, dt);

        assertFalse(result.created());
        assertEquals(new BigDecimal("70.00"), result.categoryBalance().getBalance());
    }
}
