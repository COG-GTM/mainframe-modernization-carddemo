package com.carddemo.service;

import com.carddemo.entity.TransactionType;
import com.carddemo.entity.TransactionTypeCategory;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.TransactionTypeCategoryRepository;
import com.carddemo.repository.TransactionTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionTypeService {
    private final TransactionTypeRepository typeRepository;
    private final TransactionTypeCategoryRepository categoryRepository;

    public TransactionTypeService(TransactionTypeRepository typeRepository, TransactionTypeCategoryRepository categoryRepository) {
        this.typeRepository = typeRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<TransactionType> listTransactionTypes(Pageable pageable) {
        return typeRepository.findAll(pageable);
    }

    public TransactionType getTransactionType(String code) {
        return typeRepository.findById(code)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction type not found: " + code));
    }

    @Transactional
    public TransactionType createTransactionType(TransactionType type) {
        return typeRepository.save(type);
    }

    @Transactional
    public TransactionType updateTransactionType(String code, TransactionType updated) {
        TransactionType existing = getTransactionType(code);
        if (updated.getTranTypeDesc() != null) existing.setTranTypeDesc(updated.getTranTypeDesc());
        return typeRepository.save(existing);
    }
}
