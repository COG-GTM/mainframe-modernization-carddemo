package com.cardemo.service;

import com.cardemo.entity.TransactionCategory;
import com.cardemo.entity.TransactionCategoryId;
import com.cardemo.repository.TransactionCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for TransactionCategory reference data.
 * Composite key: type code + category code.
 */
@Service
@Transactional
public class TransactionCategoryService {

    private final TransactionCategoryRepository transactionCategoryRepository;

    public TransactionCategoryService(TransactionCategoryRepository transactionCategoryRepository) {
        this.transactionCategoryRepository = transactionCategoryRepository;
    }

    @Transactional(readOnly = true)
    public Optional<TransactionCategory> findById(TransactionCategoryId id) {
        return transactionCategoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<TransactionCategory> findAll() {
        return transactionCategoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TransactionCategory> findByTypeCd(String typeCd) {
        return transactionCategoryRepository.findByTranTypeCd(typeCd);
    }

    public TransactionCategory save(TransactionCategory category) {
        return transactionCategoryRepository.save(category);
    }
}
