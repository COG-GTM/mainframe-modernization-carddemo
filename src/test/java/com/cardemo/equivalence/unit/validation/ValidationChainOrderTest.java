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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests validation chain order: 100 -> 101 -> 102 -> 103.
 * COBOL processes rules in sequence; first failure stops the chain.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Validation Chain Order Tests")
class ValidationChainOrderTest {

    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private AccountRepository accountRepository;

    private TransactionValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new TransactionValidationService(cardXrefRepository, accountRepository);
    }

    @Test
    @DisplayName("Code 100 checked before 101: card not found stops chain")
    void chainOrder_100Before101() {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("CHAIN1");
        dt.setCardNum("MISSING_CARD");
        dt.setAmount(new BigDecimal("100.00"));
        dt.setOrigTimestamp("2024-01-15-10.30.00.000000");

        when(cardXrefRepository.findById("MISSING_CARD")).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertEquals(100, result.getFailReasonCode());
        verifyNoInteractions(accountRepository);
    }

    @Test
    @DisplayName("Code 101 checked before 102: account not found stops chain")
    void chainOrder_101Before102() {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("CHAIN2");
        dt.setCardNum("VALID_CARD");
        dt.setAmount(new BigDecimal("999999.99")); // Would be overlimit
        dt.setTypeCd("SA");
        dt.setCatCd(1);
        dt.setOrigTimestamp("2024-01-15-10.30.00.000000");

        CardXref xref = new CardXref();
        xref.setCardNum("VALID_CARD");
        xref.setAcctId(999L);

        when(cardXrefRepository.findById("VALID_CARD")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        ValidationResult result = validationService.validate(dt);

        assertEquals(101, result.getFailReasonCode());
    }

    @Test
    @DisplayName("Code 102 checked before 103: overlimit stops before expiration check")
    void chainOrder_102Before103() {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("CHAIN3");
        dt.setCardNum("CARD_CHAIN");
        dt.setAmount(new BigDecimal("2000.00"));
        dt.setTypeCd("SA");
        dt.setCatCd(1);
        dt.setOrigTimestamp("2030-01-15-10.30.00.000000"); // Would also fail 103

        CardXref xref = new CardXref();
        xref.setCardNum("CARD_CHAIN");
        xref.setAcctId(300L);

        Account account = new Account();
        account.setAcctId(300L);
        account.setCreditLimit(new BigDecimal("1000.00"));
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        account.setExpirationDate("2020-01-01"); // Expired

        when(cardXrefRepository.findById("CARD_CHAIN")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(300L)).thenReturn(Optional.of(account));

        ValidationResult result = validationService.validate(dt);

        // Should get 102 (overlimit) not 103 (expired) because 102 is checked first
        assertEquals(102, result.getFailReasonCode());
    }

    @Test
    @DisplayName("All validations pass when all rules satisfied")
    void chainOrder_allPass() {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("CHAIN_OK");
        dt.setCardNum("GOOD_CARD");
        dt.setAmount(new BigDecimal("10.00"));
        dt.setTypeCd("SA");
        dt.setCatCd(1);
        dt.setOrigTimestamp("2024-01-15-10.30.00.000000");

        CardXref xref = new CardXref();
        xref.setCardNum("GOOD_CARD");
        xref.setAcctId(400L);

        Account account = new Account();
        account.setAcctId(400L);
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCurrentCycleCredit(new BigDecimal("100.00"));
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        account.setExpirationDate("2025-12-31");

        when(cardXrefRepository.findById("GOOD_CARD")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(400L)).thenReturn(Optional.of(account));

        ValidationResult result = validationService.validate(dt);

        assertTrue(result.isValid());
        assertEquals(0, result.getFailReasonCode());
    }
}
