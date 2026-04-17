package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.TransactionTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for transaction type reference data.
 *
 * COBOL Traceability: Replaces READ on TRANTYPE VSAM file.
 */
@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionTypeEntity, String> {
}
