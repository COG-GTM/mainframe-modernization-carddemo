package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.CardXref;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for CardXref entity.
 * Replaces CICS file control commands for:
 * - CCXREF VSAM file (primary key lookup by card number)
 * - CXACAIX VSAM alternate index (lookup by account ID)
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    /**
     * Find cross-reference by card number.
     * Replaces: EXEC CICS READ DATASET('CCXREF') RIDFLD(XREF-CARD-NUM)
     */
    Optional<CardXref> findByXrefCardNum(String cardNum);

    /**
     * Find cross-reference by account ID.
     * Replaces: EXEC CICS READ DATASET('CXACAIX') RIDFLD(XREF-ACCT-ID)
     */
    Optional<CardXref> findByXrefAcctId(Long acctId);
}
