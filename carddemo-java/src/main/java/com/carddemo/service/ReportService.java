package com.carddemo.service;

import com.carddemo.entity.Transaction;
import com.carddemo.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Map<String, Object> generateReport() {
        List<Transaction> transactions = transactionRepository.findAll();
        long totalCount = transactions.size();
        var byType = transactions.stream().collect(Collectors.groupingBy(
            t -> t.getTranTypeCd() != null ? t.getTranTypeCd() : "UNKNOWN", Collectors.counting()));
        return Map.of("totalTransactions", totalCount, "transactionsByType", byType);
    }
}
