package com.carddemo.card.repository;

import com.carddemo.card.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CardXref entity.
 * Translates VSAM KSDS CARDXREF file access and AIX lookup by account ID
 * from CBACT03C.cbl and cross-reference lookups used across the system.
 * This is the most critical repository — 12 programs across 5 services
 * depend on the xref lookup by account ID.
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findByXrefAcctId(String xrefAcctId);
}
