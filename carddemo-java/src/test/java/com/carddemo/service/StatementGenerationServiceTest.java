package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatementGenerationServiceTest {
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private CardRepository cardRepository;
    @InjectMocks private StatementGenerationService service;

    @Test
    void generateStatements_empty() {
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());
        List<Map<String, Object>> result = service.generateStatements();
        assertTrue(result.isEmpty());
    }

    @Test
    void generateStatements_withAccount() {
        Account account = new Account();
        account.setAcctId(10000000001L);
        account.setCurrBal(new BigDecimal("1500.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));
        account.setCurrCycCredit(BigDecimal.ZERO);
        account.setCurrCycDebit(BigDecimal.ZERO);
        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());
        when(cardRepository.findByCardAcctId(10000000001L)).thenReturn(Collections.emptyList());
        List<Map<String, Object>> result = service.generateStatements();
        assertEquals(1, result.size());
        assertEquals(10000000001L, result.get(0).get("accountId"));
    }
}
