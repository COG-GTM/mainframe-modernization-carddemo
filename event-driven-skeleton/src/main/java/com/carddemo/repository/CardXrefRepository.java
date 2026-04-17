package com.carddemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carddemo.model.CardXref;

/**
 * Repository for card cross-reference lookups.
 *
 * Replaces: XREF-FILE READ by key (FD-XREF-CARD-NUM) in CBTRN02C
 * (1500-A-LOOKUP-XREF) and sequential iteration in CBSTM03A
 * (1000-XREFFILE-GET-NEXT via CBSTM03B).
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {
}
