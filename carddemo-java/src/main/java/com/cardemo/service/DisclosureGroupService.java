package com.cardemo.service;

import com.cardemo.entity.DisclosureGroup;
import com.cardemo.entity.DisclosureGroupId;
import com.cardemo.repository.DisclosureGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for DisclosureGroup reference data.
 * Composite key: account group ID + type code + category code.
 */
@Service
@Transactional
public class DisclosureGroupService {

    private final DisclosureGroupRepository disclosureGroupRepository;

    public DisclosureGroupService(DisclosureGroupRepository disclosureGroupRepository) {
        this.disclosureGroupRepository = disclosureGroupRepository;
    }

    @Transactional(readOnly = true)
    public Optional<DisclosureGroup> findById(DisclosureGroupId id) {
        return disclosureGroupRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<DisclosureGroup> findAll() {
        return disclosureGroupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DisclosureGroup> findByAcctGroupId(String acctGroupId) {
        return disclosureGroupRepository.findByDisAcctGroupId(acctGroupId);
    }

    public DisclosureGroup save(DisclosureGroup disclosureGroup) {
        return disclosureGroupRepository.save(disclosureGroup);
    }
}
