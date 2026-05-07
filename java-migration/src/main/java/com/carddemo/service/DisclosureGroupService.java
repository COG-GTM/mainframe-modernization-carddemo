package com.carddemo.service;

import com.carddemo.entity.DisclosureGroup;
import com.carddemo.entity.DisclosureGroupId;
import com.carddemo.repository.DisclosureGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for {@link DisclosureGroup} operations.
 * Encapsulates business logic that was previously handled by
 * COBOL batch program CBACT04C.
 */
@Service
@Transactional(readOnly = true)
public class DisclosureGroupService {

    private final DisclosureGroupRepository repository;

    public DisclosureGroupService(DisclosureGroupRepository repository) {
        this.repository = repository;
    }

    public List<DisclosureGroup> findAll() {
        return repository.findAll();
    }

    public Optional<DisclosureGroup> findById(DisclosureGroupId id) {
        return repository.findById(id);
    }

    public List<DisclosureGroup> findByAccountGroupId(String accountGroupId) {
        return repository.findByIdAccountGroupId(accountGroupId);
    }

    public List<DisclosureGroup> findByTransactionTypeCode(String transactionTypeCode) {
        return repository.findByIdTransactionTypeCode(transactionTypeCode);
    }

    @Transactional
    public DisclosureGroup save(DisclosureGroup disclosureGroup) {
        return repository.save(disclosureGroup);
    }

    @Transactional
    public void deleteById(DisclosureGroupId id) {
        repository.deleteById(id);
    }

    public long count() {
        return repository.count();
    }
}
