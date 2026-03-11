package com.carddemo.service;

import com.carddemo.entity.Transaction;
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
class ReportServiceTest {
    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private ReportService reportService;

    @Test
    void generateReport_empty() {
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());
        Map<String, Object> result = reportService.generateReport();
        assertEquals(0L, result.get("totalTransactions"));
    }

    @Test
    void generateReport_withTransactions() {
        Transaction t = new Transaction();
        t.setTranTypeCd("PR");
        t.setTranAmt(new BigDecimal("100.00"));
        when(transactionRepository.findAll()).thenReturn(List.of(t));
        Map<String, Object> result = reportService.generateReport();
        assertEquals(1L, result.get("totalTransactions"));
    }
}
