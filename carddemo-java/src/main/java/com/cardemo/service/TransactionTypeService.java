package com.cardemo.service;

import com.cardemo.entity.TransactionType;
import com.cardemo.repository.TransactionTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for TransactionType reference data.
 * Read-only reference table mapping type codes to descriptions.
 */
@Service
@Transactional
public class TransactionTypeService {

    private final TransactionTypeRepository transactionTypeRepository;

    public TransactionTypeService(TransactionTypeRepository transactionTypeRepository) {
        this.transactionTypeRepository = transactionTypeRepository;
    }

    @Transactional(readOnly = true)
    public Optional<TransactionType> findById(String tranType) {
        return transactionTypeRepository.findById(tranType);
    }

    @Transactional(readOnly = true)
    public List<TransactionType> findAll() {
        return transactionTypeRepository.findAll();
    }

    public TransactionType save(TransactionType transactionType) {
        return transactionTypeRepository.save(transactionType);
    }
}
