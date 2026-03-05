package com.carddemo.service;

import com.carddemo.entity.TransactionType;
import com.carddemo.repository.TransactionTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transaction type management service — replaces COTRTLIC.cbl and COTRTUPC.cbl.
 * Manages transaction type reference data (DB2 cursor paging → JPA pagination).
 */
@Service
public class TransactionTypeService {

    private final TransactionTypeRepository transactionTypeRepository;

    public TransactionTypeService(TransactionTypeRepository transactionTypeRepository) {
        this.transactionTypeRepository = transactionTypeRepository;
    }

    public Page<TransactionType> listTransactionTypes(Pageable pageable) {
        return transactionTypeRepository.findAll(pageable);
    }

    public TransactionType getTransactionType(String typeCode) {
        return transactionTypeRepository.findById(typeCode)
                .orElseThrow(() -> new IllegalArgumentException("Transaction type not found: " + typeCode));
    }

    @Transactional
    public TransactionType addTransactionType(TransactionType type) {
        if (transactionTypeRepository.existsById(type.getTypeCode())) {
            throw new IllegalArgumentException("Transaction type already exists: " + type.getTypeCode());
        }
        return transactionTypeRepository.save(type);
    }

    @Transactional
    public TransactionType updateTransactionType(String typeCode, TransactionType dto) {
        TransactionType existing = transactionTypeRepository.findById(typeCode)
                .orElseThrow(() -> new IllegalArgumentException("Transaction type not found: " + typeCode));
        existing.setTypeDescription(dto.getTypeDescription());
        return transactionTypeRepository.save(existing);
    }

    @Transactional
    public void deleteTransactionType(String typeCode) {
        if (!transactionTypeRepository.existsById(typeCode)) {
            throw new IllegalArgumentException("Transaction type not found: " + typeCode);
        }
        transactionTypeRepository.deleteById(typeCode);
    }
}
