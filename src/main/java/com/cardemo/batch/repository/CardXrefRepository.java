package com.cardemo.batch.repository;

import com.cardemo.batch.model.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for XREFFILE (card cross-reference) records.
 * Supports lookup by ACCT-ID (alternate key in VSAM).
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    Optional<CardXref> findFirstByAcctId(String acctId);
}
