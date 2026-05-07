package com.carddemo.service;

import com.carddemo.entity.TransactionType;
import com.carddemo.repository.TransactionTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for {@link TransactionType} operations.
 *
 * <p>Encapsulates business logic for transaction type management,
 * migrated from COBOL batch program CBTRN03C.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionTypeService {

    private final TransactionTypeRepository repository;

    public List<TransactionType> findAll() {
        return repository.findAll();
    }

    public Optional<TransactionType> findByTypeCode(String typeCode) {
        return repository.findById(typeCode);
    }

    public List<TransactionType> findByDescriptionContaining(String description) {
        return repository.findByDescriptionContaining(description);
    }

    @Transactional
    public TransactionType save(TransactionType transactionType) {
        return repository.save(transactionType);
    }

    @Transactional
    public void deleteByTypeCode(String typeCode) {
        repository.deleteById(typeCode);
    }
}
