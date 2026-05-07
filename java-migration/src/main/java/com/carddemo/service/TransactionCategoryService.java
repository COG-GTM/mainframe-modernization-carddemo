package com.carddemo.service;

import com.carddemo.entity.TransactionCategory;
import com.carddemo.entity.TransactionCategoryId;
import com.carddemo.repository.TransactionCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for TransactionCategory operations.
 *
 * <p>Encapsulates business logic for transaction category management,
 * migrated from COBOL program CBTRN03C.cbl batch processing.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionCategoryService {

    private final TransactionCategoryRepository repository;

    public List<TransactionCategory> findAll() {
        return repository.findAll();
    }

    public Optional<TransactionCategory> findById(TransactionCategoryId id) {
        return repository.findById(id);
    }

    public Optional<TransactionCategory> findById(String typeCode, Integer categoryCode) {
        return repository.findById(new TransactionCategoryId(typeCode, categoryCode));
    }

    public List<TransactionCategory> findByTypeCode(String typeCode) {
        return repository.findByIdTypeCode(typeCode);
    }

    public List<TransactionCategory> searchByDescription(String keyword) {
        return repository.findByDescriptionContainingIgnoreCase(keyword);
    }

    @Transactional
    public TransactionCategory save(TransactionCategory category) {
        return repository.save(category);
    }

    @Transactional
    public void deleteById(TransactionCategoryId id) {
        repository.deleteById(id);
    }

    public long count() {
        return repository.count();
    }
}
