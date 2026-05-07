package com.carddemo.service;

import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.entity.TransactionCategoryBalanceId;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TransactionCategoryBalanceService}.
 * Validates business logic previously in COBOL batch programs CBACT04C and CBTRN02C.
 */
@ExtendWith(MockitoExtension.class)
class TransactionCategoryBalanceServiceTest {

    @Mock
    private TransactionCategoryBalanceRepository repository;

    @InjectMocks
    private TransactionCategoryBalanceService service;

    private TransactionCategoryBalanceId testId;
    private TransactionCategoryBalance testEntity;

    @BeforeEach
    void setUp() {
        testId = new TransactionCategoryBalanceId(1L, "01", 1);
        testEntity = new TransactionCategoryBalance(testId, new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("findById: delegates to repository")
    void testFindById() {
        when(repository.findById(testId)).thenReturn(Optional.of(testEntity));

        Optional<TransactionCategoryBalance> result = service.findById(testId);

        assertTrue(result.isPresent());
        assertEquals(testEntity, result.get());
        verify(repository).findById(testId);
    }

    @Test
    @DisplayName("findByAccountId: delegates to repository custom finder")
    void testFindByAccountId() {
        when(repository.findByIdAccountId(1L)).thenReturn(List.of(testEntity));

        List<TransactionCategoryBalance> results = service.findByAccountId(1L);

        assertEquals(1, results.size());
        assertEquals(testEntity, results.get(0));
        verify(repository).findByIdAccountId(1L);
    }

    @Test
    @DisplayName("findByTypeCode: delegates to repository custom finder")
    void testFindByTypeCode() {
        when(repository.findByIdTypeCode("01")).thenReturn(List.of(testEntity));

        List<TransactionCategoryBalance> results = service.findByTypeCode("01");

        assertEquals(1, results.size());
        verify(repository).findByIdTypeCode("01");
    }

    @Test
    @DisplayName("addToBalance: adds amount to existing record (mirrors CBTRN02C 2700-B)")
    void testAddToBalanceExistingRecord() {
        when(repository.findById(testId)).thenReturn(Optional.of(testEntity));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionCategoryBalance result = service.addToBalance(testId, new BigDecimal("50.00"));

        assertEquals(0, new BigDecimal("150.00").compareTo(result.getBalance()));
        verify(repository).save(any());
    }

    @Test
    @DisplayName("addToBalance: creates new record when not found (mirrors CBTRN02C 2700-A)")
    void testAddToBalanceNewRecord() {
        when(repository.findById(testId)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionCategoryBalance result = service.addToBalance(testId, new BigDecimal("75.00"));

        assertEquals(0, new BigDecimal("75.00").compareTo(result.getBalance()));
        verify(repository).save(any());
    }

    @Test
    @DisplayName("addToBalance: handles negative amounts (debit transactions)")
    void testAddToBalanceNegativeAmount() {
        when(repository.findById(testId)).thenReturn(Optional.of(testEntity));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionCategoryBalance result = service.addToBalance(testId, new BigDecimal("-30.00"));

        assertEquals(0, new BigDecimal("70.00").compareTo(result.getBalance()));
    }

    @Test
    @DisplayName("save: delegates to repository")
    void testSave() {
        when(repository.save(testEntity)).thenReturn(testEntity);

        TransactionCategoryBalance result = service.save(testEntity);

        assertEquals(testEntity, result);
        verify(repository).save(testEntity);
    }

    @Test
    @DisplayName("deleteById: delegates to repository")
    void testDeleteById() {
        doNothing().when(repository).deleteById(testId);

        service.deleteById(testId);

        verify(repository).deleteById(testId);
    }
}
