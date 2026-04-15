package com.carddemo.card.repository;

import com.carddemo.card.model.CardCrossReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CardCrossReference entities.
 * Mirrors the VSAM XREFFILE (CARDXREF) access used in COCRDSLC.cbl and COCRDUPC.cbl
 * for resolving card-to-customer and card-to-account linkages.
 */
@Repository
public interface CardCrossReferenceRepository extends JpaRepository<CardCrossReference, String> {

    /**
     * Find all cross-references for a given account ID.
     */
    List<CardCrossReference> findByAccountId(String accountId);
}
