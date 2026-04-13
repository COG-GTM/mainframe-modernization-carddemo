package com.cardemo.service;

import com.cardemo.entity.TransactionCategoryBalance;
import com.cardemo.entity.TransactionCategoryBalanceId;
import com.cardemo.repository.TransactionCategoryBalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for TransactionCategoryBalance entity.
 * Tracks per-account balances by transaction type/category.
 * Mirrors COBOL operations in CBACT04C (interest calc) and CBTRN02C (batch post).
 */
@Service
@Transactional
public class TransactionCategoryBalanceService {

    private final TransactionCategoryBalanceRepository repository;

    public TransactionCategoryBalanceService(TransactionCategoryBalanceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<TransactionCategoryBalance> findById(TransactionCategoryBalanceId id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<TransactionCategoryBalance> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TransactionCategoryBalance> findByAcctId(Long acctId) {
        return repository.findByTrancatAcctId(acctId);
    }

    public TransactionCategoryBalance save(TransactionCategoryBalance balance) {
        return repository.save(balance);
    }

    public TransactionCategoryBalance update(TransactionCategoryBalance balance) {
        return repository.save(balance);
    }
}
