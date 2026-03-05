package com.carddemo.batch;

import com.carddemo.batch.posttran.TransactionPostingProcessor;
import com.carddemo.batch.posttran.TransactionPostingResult;
import com.carddemo.entity.*;
import com.carddemo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionPostingProcessor — validates parity with CBTRN02C.cbl.
 * Tests all four reject codes (100, 101, 102, 103) and successful posting.
 */
@ExtendWith(MockitoExtension.class)
class TransactionPostingProcessorTest {

    @Mock private CardXrefRepository cardXrefRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionCategoryBalanceRepository tcbRepository;

    private TransactionPostingProcessor processor;

    private DailyTransaction validTransaction;
    private CardXref cardXref;
    private Account account;

    @BeforeEach
    void setUp() {
        processor = new TransactionPostingProcessor(cardXrefRepository, accountRepository, transactionRepository, tcbRepository);

        validTransaction = new DailyTransaction();
        validTransaction.setTransactionId("TXN0000000000001");
        validTransaction.setCardNum("4111111111111111");
        validTransaction.setAmount(new BigDecimal("100.00"));
        validTransaction.setTypeCd("01");
        validTransaction.setCategoryCd(1);
        validTransaction.setOrigTimestamp(LocalDateTime.of(2024, 6, 15, 10, 30));

        cardXref = new CardXref();
        cardXref.setCardNum("4111111111111111");
        cardXref.setAcctId(1L);
        cardXref.setCustId(100L);

        account = new Account();
        account.setAcctId(1L);
        account.setCurrentBalance(new BigDecimal("1000.00"));
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCurrentCycleCredit(new BigDecimal("200.00"));
        account.setCurrentCycleDebit(new BigDecimal("100.00"));
        account.setExpirationDate(LocalDate.of(2025, 12, 31));
    }

    @Test
    void process_validTransaction_createsTransaction() throws Exception {
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(cardXref));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(tcbRepository.findById(any())).thenReturn(Optional.empty());
        when(tcbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionPostingResult result = processor.process(validTransaction);

        assertNotNull(result);
        assertTrue(result.isValid());
        assertNotNull(result.getPostedTransaction());
        assertNull(result.getReject());
    }

    @Test
    void process_cardNotFound_rejectCode100() throws Exception {
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.empty());

        TransactionPostingResult result = processor.process(validTransaction);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertNull(result.getPostedTransaction());
        assertNotNull(result.getReject());
        assertEquals(100, result.getReject().getRejectReasonCode());
    }

    @Test
    void process_accountNotFound_rejectCode101() throws Exception {
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(cardXref));
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        TransactionPostingResult result = processor.process(validTransaction);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getReject());
        assertEquals(101, result.getReject().getRejectReasonCode());
    }

    @Test
    void process_exceedsCreditLimit_rejectCode102() throws Exception {
        account.setCreditLimit(new BigDecimal("200.00"));
        account.setCurrentCycleCredit(new BigDecimal("150.00"));
        account.setCurrentCycleDebit(new BigDecimal("0.00"));

        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(cardXref));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        TransactionPostingResult result = processor.process(validTransaction);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getReject());
        assertEquals(102, result.getReject().getRejectReasonCode());
    }

    @Test
    void process_expiredAccount_rejectCode103() throws Exception {
        account.setExpirationDate(LocalDate.of(2024, 1, 1));

        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(cardXref));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        TransactionPostingResult result = processor.process(validTransaction);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getReject());
        assertEquals(103, result.getReject().getRejectReasonCode());
    }
}
