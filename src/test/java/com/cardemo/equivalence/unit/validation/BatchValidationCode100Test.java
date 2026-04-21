package com.cardemo.equivalence.unit.validation;

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
 * Tests for validation rule Code 100: INVALID CARD NUMBER FOUND.
 * Verifies character-for-character error message matching with COBOL.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Batch Validation Rule - Code 100: Invalid Card Number")
class BatchValidationCode100Test {

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
        dt.setId("TXN001");
        dt.setCardNum(cardNum);
        dt.setAmount(new BigDecimal("100.00"));
        dt.setTypeCd("SA");
        dt.setCatCd(1);
        dt.setOrigTimestamp("2024-01-15-10.30.00.000000");
        return dt;
    }

    @Test
    @DisplayName("Should reject with code 100 when card number not found in XREF")
    void validate_cardNotFound_returnsCode100() {
        DailyTransaction dt = createTransaction("9999999999999999");
        when(cardXrefRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(100, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Should match COBOL error message character-for-character: 'INVALID CARD NUMBER FOUND'")
    void validate_cardNotFound_exactErrorMessage() {
        DailyTransaction dt = createTransaction("0000000000000000");
        when(cardXrefRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertEquals("INVALID CARD NUMBER FOUND", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("Should reject when card number is empty string")
    void validate_emptyCardNum_returnsCode100() {
        DailyTransaction dt = createTransaction("");
        when(cardXrefRepository.findById("")).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(100, result.getFailReasonCode());
        assertEquals("INVALID CARD NUMBER FOUND", result.getFailReasonDescription());
    }

    @Test
    @DisplayName("Should reject when card number has invalid format")
    void validate_invalidFormatCardNum_returnsCode100() {
        DailyTransaction dt = createTransaction("ABCDEFGHIJKLMNOP");
        when(cardXrefRepository.findById("ABCDEFGHIJKLMNOP")).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertFalse(result.isValid());
        assertEquals(100, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Should pass validation when card number exists in XREF and account is valid")
    void validate_validCardNum_passes() {
        DailyTransaction dt = createTransaction("1234567890123456");
        CardXref xref = new CardXref();
        xref.setCardNum("1234567890123456");
        xref.setAcctId(100001L);

        Account account = new Account();
        account.setAcctId(100001L);
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCurrentCycleCredit(new BigDecimal("100.00"));
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        account.setExpirationDate("2025-12-31");

        when(cardXrefRepository.findById("1234567890123456")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(100001L)).thenReturn(Optional.of(account));

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should only query XREF repository, not Account, when card not found")
    void validate_cardNotFound_doesNotQueryAccount() {
        DailyTransaction dt = createTransaction("UNKNOWN_CARD_NUM");
        when(cardXrefRepository.findById("UNKNOWN_CARD_NUM")).thenReturn(Optional.empty());

        validationService.validate(dt);

        verify(cardXrefRepository).findById("UNKNOWN_CARD_NUM");
        verifyNoInteractions(accountRepository);
    }
}
