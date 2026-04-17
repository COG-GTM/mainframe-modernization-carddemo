package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.DisclosureGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for disclosure group (interest rate) data.
 *
 * COBOL Traceability: Replaces READ on DISCGRP VSAM file
 * used by CBACT04C for interest rate lookup.
 */
@Repository
public interface DisclosureGroupRepository
        extends JpaRepository<DisclosureGroupEntity, DisclosureGroupEntity.DisclosureGroupId> {

    Optional<DisclosureGroupEntity> findByAccountGroupIdAndTransactionTypeCodeAndTransactionCategoryCode(
            String accountGroupId, String transactionTypeCode, int transactionCategoryCode);
}
