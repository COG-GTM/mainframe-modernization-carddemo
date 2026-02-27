package com.carddemo.service;

import com.carddemo.entity.*;
import com.carddemo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionPostingServiceTest {
    @Mock private DailyTransactionRepository dailyTransactionRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private CardAccountXrefRepository xrefRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionCategoryBalanceRepository tcbRepository;
    @InjectMocks private TransactionPostingService service;

    @Test
    void postDailyTransactions_empty() {
        when(dailyTransactionRepository.findAll()).thenReturn(Collections.emptyList());
        TransactionPostingService.PostingResult result = service.postDailyTransactions();
        assertEquals(0, result.getPosted());
        assertEquals(0, result.getRejected());
    }

    @Test
    void postDailyTransactions_success() {
        DailyTransaction dt = new DailyTransaction();
        dt.setTranId("TRAN001");
        dt.setTranCardNum("4111111111111111");
        dt.setTranTypeCd("PR");
        dt.setTranCatCd(5000);
        dt.setTranAmt(new BigDecimal("100.00"));

        CardAccountXref xref = new CardAccountXref();
        xref.setAcctId(10000000001L);

        Account account = new Account();
        account.setAcctId(10000000001L);
        account.setActiveStatus("Y");
        account.setCurrBal(new BigDecimal("1500.00"));
        account.setCurrCycDebit(BigDecimal.ZERO);
        account.setCurrCycCredit(BigDecimal.ZERO);

        when(dailyTransactionRepository.findAll()).thenReturn(List.of(dt));
        when(xrefRepository.findByCardNum("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(10000000001L)).thenReturn(Optional.of(account));
        when(tcbRepository.findById(any())).thenReturn(Optional.empty());

        TransactionPostingService.PostingResult result = service.postDailyTransactions();
        assertEquals(1, result.getPosted());
        assertEquals(0, result.getRejected());
    }

    @Test
    void postDailyTransactions_rejected() {
        DailyTransaction dt = new DailyTransaction();
        dt.setTranId("TRAN001");
        dt.setTranCardNum("UNKNOWN_CARD");
        dt.setTranAmt(new BigDecimal("100.00"));

        when(dailyTransactionRepository.findAll()).thenReturn(List.of(dt));
        when(xrefRepository.findByCardNum("UNKNOWN_CARD")).thenReturn(Optional.empty());

        TransactionPostingService.PostingResult result = service.postDailyTransactions();
        assertEquals(0, result.getPosted());
        assertEquals(1, result.getRejected());
    }
}
