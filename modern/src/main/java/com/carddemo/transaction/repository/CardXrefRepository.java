package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.CardXrefEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for card cross-reference data access.
 *
 * COBOL Traceability: Replaces EXEC CICS READ on CCXREF and CXACAIX files.
 * CXACAIX is an alternate index on CARDXREF by account ID.
 * Used by COTRN02C and COBIL00C for card-account validation.
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXrefEntity, String> {

    Optional<CardXrefEntity> findByAccountId(String accountId);
}
