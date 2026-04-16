package com.cardemo.equivalence.unit.validation;

import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.ValidationResult;
import com.cardemo.batch.repository.AccountRepository;
import com.cardemo.batch.repository.CardXrefRepository;
import com.cardemo.batch.service.TransactionValidationService;
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
 * Tests for validation rule Code 101: ACCOUNT RECORD NOT FOUND.
 * Verifies character-for-character error message matching with COBOL.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Batch Validation Rule - Code 101: Account Not Found")
class BatchValidationCode101Test {

    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private AccountRepository accountRepository;

    private TransactionValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new TransactionValidationService(cardXrefRepository, accountRepository);
    }

    private DailyTransaction createTransaction(String cardNum) {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("TXN002");
        dt.setCardNum(cardNum);
        dt.setAmount(new BigDecimal("50.00"));
        dt.setTypeCd("SA");
        dt.setCatCd(1);
        dt.setOrigTimestamp("2024-01-15-10.30.00.000000");
        return dt;
    }

    @Test
    @DisplayName("Should reject with code 101 when account not found for valid card")
    void validate_accountNotFound_returnsCode101() {
        DailyTransaction dt = createTransaction("1234567890123456");
        CardXref xref = new CardXref();
        xref.setCardNum("1234567890123456");
        xref.setAcctId(999999L);

        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(999999L)).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(101, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Should match COBOL error message character-for-character: 'ACCOUNT RECORD NOT FOUND'")
    void validate_accountNotFound_exactErrorMessage() {
        DailyTransaction dt = createTransaction("1234567890123456");
        CardXref xref = new CardXref();
        xref.setCardNum("1234567890123456");
        xref.setAcctId(888888L);

        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(888888L)).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertEquals("ACCOUNT RECORD NOT FOUND", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("Should query account repository with correct account ID from XREF")
    void validate_accountNotFound_queriesCorrectAcctId() {
        DailyTransaction dt = createTransaction("1111222233334444");
        CardXref xref = new CardXref();
        xref.setCardNum("1111222233334444");
        xref.setAcctId(123456L);

        when(cardXrefRepository.findById("1111222233334444")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(123456L)).thenReturn(Optional.empty());

        validationService.validate(dt);

        verify(accountRepository).findById(123456L);
    }

    @Test
    @DisplayName("Should reject with code 101 for different XREF mappings")
    void validate_differentXrefMapping_returnsCode101() {
        DailyTransaction dt = createTransaction("5555666677778888");
        CardXref xref = new CardXref();
        xref.setCardNum("5555666677778888");
        xref.setAcctId(42L);

        when(cardXrefRepository.findById("5555666677778888")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(42L)).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(101, result.getFailReasonCode());
        assertEquals("ACCOUNT RECORD NOT FOUND", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("Code 101 takes precedence over code 102/103 when account missing")
    void validate_accountNotFound_takePrecedenceOverOtherCodes() {
        DailyTransaction dt = createTransaction("CARD_FOR_101_TEST");
        dt.setAmount(new BigDecimal("999999.99")); // Would be overlimit if account existed
        CardXref xref = new CardXref();
        xref.setCardNum("CARD_FOR_101_TEST");
        xref.setAcctId(777L);

        when(cardXrefRepository.findById("CARD_FOR_101_TEST")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(777L)).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertEquals(101, result.getFailReasonCode());
    }
}
