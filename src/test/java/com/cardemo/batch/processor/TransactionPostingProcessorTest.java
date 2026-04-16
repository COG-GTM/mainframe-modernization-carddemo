package com.cardemo.batch.processor;

import com.cardemo.batch.model.Account;
import com.cardemo.batch.model.CardXref;
import com.cardemo.batch.model.DailyTransaction;
import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionPostingResult;
import com.cardemo.batch.model.ValidationResult;
import com.cardemo.batch.service.AccountUpdateService;
import com.cardemo.batch.service.TimestampService;
import com.cardemo.batch.service.TransactionCategoryBalanceService;
import com.cardemo.batch.service.TransactionValidationService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionPostingProcessor.
 * Tests the main processing logic from CBTRN02C.
 */
@ExtendWith(MockitoExtension.class)
class TransactionPostingProcessorTest {

    @Mock
    private TransactionValidationService validationService;

    @Mock
    private AccountUpdateService accountUpdateService;

    @Mock
    private TransactionCategoryBalanceService categoryBalanceService;

    @Mock
    private TimestampService timestampService;

    private MeterRegistry meterRegistry;
    private TransactionPostingProcessor processor;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        processor = new TransactionPostingProcessor(
                validationService, accountUpdateService, categoryBalanceService,
                timestampService, meterRegistry);
    }

    private DailyTransaction createDailyTransaction() {
        DailyTransaction dt = new DailyTransaction();
        dt.setId("TRAN0000000001");
        dt.setTypeCd("SA");
        dt.setCatCd(5001);
        dt.setSource("ONLINE");
        dt.setDescription("Test purchase");
        dt.setAmount(new BigDecimal("100.00"));
        dt.setMerchantId(123456789L);
        dt.setMerchantName("TEST STORE");
        dt.setMerchantCity("NEW YORK");
        dt.setMerchantZip("10001");
        dt.setCardNum("1234567890123456");
        dt.setOrigTimestamp("2025-01-15-10.30.00.000000");
        return dt;
    }

    @Test
    @DisplayName("Rejected transaction increments reject counter and returns RejectedTransaction")
    void process_invalidTransaction_returnsRejection() {
        DailyTransaction dt = createDailyTransaction();
        when(validationService.validate(dt))
                .thenReturn(ValidationResult.failure(100, "INVALID CARD NUMBER FOUND"));

        TransactionPostingResult result = processor.process(dt);

        assertFalse(result.isPosted());
        assertNotNull(result.getRejectedTransaction());
        assertEquals(100, result.getRejectedTransaction().getFailReasonCode());
        assertEquals("INVALID CARD NUMBER FOUND", result.getRejectedTransaction().getFailReasonDescription());
        assertEquals(1.0, meterRegistry.find("cardemo.batch.transactions.rejected").counter().count());
    }

    @Test
    @DisplayName("Valid transaction returns posted Transaction with proc timestamp set")
    void process_validTransaction_returnsPosted() {
        DailyTransaction dt = createDailyTransaction();
        CardXref xref = new CardXref();
        xref.setCardNum("1234567890123456");
        xref.setAcctId(12345678901L);
        Account acct = new Account();
        acct.setAcctId(12345678901L);
        acct.setCurrentBalance(BigDecimal.ZERO);
        acct.setCurrentCycleCredit(BigDecimal.ZERO);
        acct.setCurrentCycleDebit(BigDecimal.ZERO);
        TransactionCategoryBalance catBal = new TransactionCategoryBalance();
        catBal.setBalance(new BigDecimal("100.00"));

        when(validationService.validate(dt)).thenReturn(ValidationResult.success());
        when(validationService.lookupXref("1234567890123456")).thenReturn(Optional.of(xref));
        when(validationService.lookupAccount(12345678901L)).thenReturn(Optional.of(acct));
        when(timestampService.generateDb2Timestamp()).thenReturn("2025-01-15-10.35.00.000000");
        when(categoryBalanceService.updateCategoryBalance(eq(12345678901L), eq(dt)))
                .thenReturn(new TransactionCategoryBalanceService.UpdateResult(catBal, true));

        TransactionPostingResult result = processor.process(dt);

        assertTrue(result.isPosted());
        assertEquals("TRAN0000000001", result.getPostedTransaction().getId());
        assertEquals("2025-01-15-10.35.00.000000", result.getPostedTransaction().getProcTimestamp());
        assertEquals(new BigDecimal("100.00"), result.getPostedTransaction().getAmount());
    }

    @Test
    @DisplayName("Transaction counter increments for every processed transaction")
    void process_incrementsTransactionCounter() {
        DailyTransaction dt = createDailyTransaction();
        when(validationService.validate(dt))
                .thenReturn(ValidationResult.failure(100, "INVALID CARD NUMBER FOUND"));

        processor.process(dt);
        processor.process(dt);

        assertEquals(2.0, meterRegistry.find("cardemo.batch.transactions.total").counter().count());
    }

    @Test
    @DisplayName("Reject counter only increments for rejected transactions")
    void process_rejectCounterOnlyForRejections() {
        DailyTransaction dt = createDailyTransaction();
        CardXref xref = new CardXref();
        xref.setCardNum("1234567890123456");
        xref.setAcctId(12345678901L);
        Account acct = new Account();
        acct.setAcctId(12345678901L);
        acct.setCurrentBalance(BigDecimal.ZERO);
        acct.setCurrentCycleCredit(BigDecimal.ZERO);
        acct.setCurrentCycleDebit(BigDecimal.ZERO);
        TransactionCategoryBalance catBal = new TransactionCategoryBalance();
        catBal.setBalance(new BigDecimal("100.00"));

        when(validationService.validate(dt)).thenReturn(ValidationResult.success());
        when(validationService.lookupXref("1234567890123456")).thenReturn(Optional.of(xref));
        when(validationService.lookupAccount(12345678901L)).thenReturn(Optional.of(acct));
        when(timestampService.generateDb2Timestamp()).thenReturn("2025-01-15-10.35.00.000000");
        when(categoryBalanceService.updateCategoryBalance(eq(12345678901L), eq(dt)))
                .thenReturn(new TransactionCategoryBalanceService.UpdateResult(catBal, false));

        processor.process(dt);

        assertEquals(0.0, meterRegistry.find("cardemo.batch.transactions.rejected").counter().count());
    }
}
