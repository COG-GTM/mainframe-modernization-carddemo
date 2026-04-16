package com.carddemo.transaction.service;

import com.carddemo.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Component;

/**
 * Sequential transaction ID generator.
 * Port of COBOL pattern: READPREV from HIGH-VALUES to get last ID, then add 1.
 * IDs are 16-character zero-padded strings (matching PIC X(16) / PIC 9(16)).
 */
@Component
public class TransactionIdGenerator {

    private final TransactionRepository transactionRepository;

    public TransactionIdGenerator(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Generates the next sequential transaction ID.
     * Reads the last (highest) transaction ID and increments by 1.
     * If no transactions exist, starts at 1.
     */
    public String generateNextId() {
        return transactionRepository.findFirstByOrderByTranIdDesc()
                .map(lastTran -> {
                    String lastId = lastTran.getTranId().trim();
                    long lastNum = Long.parseLong(lastId);
                    return formatId(lastNum + 1);
                })
                .orElse(formatId(1L));
    }

    private String formatId(long id) {
        return String.format("%016d", id);
    }
}
