package com.cardemo.service;

import com.cardemo.entity.Transaction;
import com.cardemo.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Transaction entity operations.
 * Mirrors COBOL operations: keyed READ, sequential READ (STARTBR/READNEXT/READPREV), WRITE, REWRITE.
 */
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> findById(String tranId) {
        return transactionRepository.findById(tranId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Transaction update(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByCardNum(String cardNum) {
        return transactionRepository.findByTranCardNum(cardNum);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByTypeCd(String typeCd) {
        return transactionRepository.findByTranTypeCd(typeCd);
    }
}
