package com.cardemo.service;

import com.cardemo.entity.DailyTransaction;
import com.cardemo.repository.DailyTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for DailyTransaction entity operations.
 * Mirrors COBOL operations: sequential READ, WRITE, REWRITE.
 */
@Service
@Transactional
public class DailyTransactionService {

    private final DailyTransactionRepository dailyTransactionRepository;

    public DailyTransactionService(DailyTransactionRepository dailyTransactionRepository) {
        this.dailyTransactionRepository = dailyTransactionRepository;
    }

    @Transactional(readOnly = true)
    public Optional<DailyTransaction> findById(String dalytranId) {
        return dailyTransactionRepository.findById(dalytranId);
    }

    @Transactional(readOnly = true)
    public List<DailyTransaction> findAll() {
        return dailyTransactionRepository.findAll();
    }

    public DailyTransaction save(DailyTransaction dailyTransaction) {
        return dailyTransactionRepository.save(dailyTransaction);
    }

    public DailyTransaction update(DailyTransaction dailyTransaction) {
        return dailyTransactionRepository.save(dailyTransaction);
    }

    @Transactional(readOnly = true)
    public List<DailyTransaction> findByCardNum(String cardNum) {
        return dailyTransactionRepository.findByDalytranCardNum(cardNum);
    }
}
