package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.CardCrossReference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CardCrossReference entity.
 *
 * Replaces two CICS file control operations:
 *   - READ on CCXREF (by card number) -> findByCardNumber / findById
 *   - READ on CXACAIX (by account ID) -> findByAccountId
 *
 * The CXACAIX alternate index is replaced by a database index on account_id.
 */
@Repository
public interface CardCrossReferenceRepository extends JpaRepository<CardCrossReference, String> {

    /**
     * Lookup by card number (primary key).
     * Replaces: EXEC CICS READ DATASET(WS-CCXREF-FILE) RIDFLD(XREF-CARD-NUM)
     */
    Optional<CardCrossReference> findByCardNumber(String cardNumber);

    /**
     * Lookup by account ID (alternate index).
     * Replaces: EXEC CICS READ DATASET(WS-CXACAIX-FILE) RIDFLD(XREF-ACCT-ID)
     */
    Optional<CardCrossReference> findFirstByAccountId(Long accountId);
}
