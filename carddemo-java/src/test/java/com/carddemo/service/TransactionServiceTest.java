package com.carddemo.service;

import com.carddemo.dto.TransactionRequest;
import com.carddemo.entity.*;
import com.carddemo.exception.BusinessException;
import com.carddemo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock private TransactionRepository transactionRepository;
    @Mock private CardAccountXrefRepository xrefRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionCategoryBalanceRepository tcbRepository;
    @InjectMocks private TransactionService transactionService;
    private Account testAccount;
    private CardAccountXref testXref;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAcctId(10000000001L);
        testAccount.setActiveStatus("Y");
        testAccount.setCurrBal(new BigDecimal("1500.00"));
        testAccount.setCreditLimit(new BigDecimal("10000.00"));
        testAccount.setCurrCycDebit(BigDecimal.ZERO);
        testAccount.setCurrCycCredit(BigDecimal.ZERO);

        testXref = new CardAccountXref();
        testXref.setCardNum("4111111111111111");
        testXref.setAcctId(10000000001L);
    }

    @Test
    void listTransactions() {
        Page<Transaction> page = new PageImpl<>(List.of());
        when(transactionRepository.findAll(any(PageRequest.class))).thenReturn(page);
        Page<Transaction> result = transactionService.listTransactions(PageRequest.of(0, 10));
        assertNotNull(result);
    }

    @Test
    void getTransaction_notFound() {
        when(transactionRepository.findById("INVALID")).thenReturn(Optional.empty());
        assertThrows(com.carddemo.exception.ResourceNotFoundException.class, () -> transactionService.getTransaction("INVALID"));
    }

    @Test
    void createTransaction_success() {
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(testXref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(tcbRepository.findById(any())).thenReturn(Optional.empty());
        when(tcbRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TransactionRequest req = new TransactionRequest();
        req.setCardNum("4111111111111111");
        req.setTranTypeCd("PR");
        req.setTranCatCd(5000);
        req.setTranAmt(new BigDecimal("100.00"));
        Transaction result = transactionService.createTransaction(req);
        assertNotNull(result);
        assertEquals("PR", result.getTranTypeCd());
    }

    @Test
    void createTransaction_cardNotFound() {
        when(xrefRepository.findByCardNum("0000")).thenReturn(Optional.empty());
        TransactionRequest req = new TransactionRequest();
        req.setCardNum("0000");
        req.setTranTypeCd("PR");
        req.setTranCatCd(5000);
        req.setTranAmt(new BigDecimal("100.00"));
        BusinessException ex = assertThrows(BusinessException.class, () -> transactionService.createTransaction(req));
        assertEquals("4100", ex.getErrorCode());
    }

    @Test
    void createTransaction_accountInactive() {
        testAccount.setActiveStatus("N");
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(testXref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(testAccount));
        TransactionRequest req = new TransactionRequest();
        req.setCardNum("4111111111111111");
        req.setTranTypeCd("PR");
        req.setTranCatCd(5000);
        req.setTranAmt(new BigDecimal("100.00"));
        BusinessException ex = assertThrows(BusinessException.class, () -> transactionService.createTransaction(req));
        assertEquals("4300", ex.getErrorCode());
    }

    @Test
    void createTransaction_exceedsCreditLimit() {
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(testXref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(testAccount));
        TransactionRequest req = new TransactionRequest();
        req.setCardNum("4111111111111111");
        req.setTranTypeCd("PR");
        req.setTranCatCd(5000);
        req.setTranAmt(new BigDecimal("9000.00"));
        BusinessException ex = assertThrows(BusinessException.class, () -> transactionService.createTransaction(req));
        assertEquals("5100", ex.getErrorCode());
    }
}
