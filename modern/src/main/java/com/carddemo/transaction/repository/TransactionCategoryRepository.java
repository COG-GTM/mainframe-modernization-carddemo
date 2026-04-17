package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.TransactionCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for transaction category reference data.
 *
 * COBOL Traceability: Replaces READ on TRANCATG VSAM file.
 */
@Repository
public interface TransactionCategoryRepository
        extends JpaRepository<TransactionCategoryEntity, TransactionCategoryEntity.TransactionCategoryId> {
}
