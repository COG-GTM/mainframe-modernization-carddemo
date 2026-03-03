package com.carddemo.posttran.repository;

import com.carddemo.posttran.model.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for the card_xref table (maps to XREFFILE / CVACT03Y).
 * Provides card-number-to-account cross-reference lookups.
 * Maps to COBOL section 1500-A-LOOKUP-XREF.
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    /**
     * Look up the cross-reference record by card number.
     * In COBOL: MOVE DALYTRAN-CARD-NUM TO FD-XREF-CARD-NUM / READ XREF-FILE
     */
    Optional<CardXref> findByXrefCardNum(String cardNum);
}
