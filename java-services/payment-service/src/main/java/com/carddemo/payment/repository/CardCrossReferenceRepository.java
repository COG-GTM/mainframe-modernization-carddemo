package com.carddemo.payment.repository;

import com.carddemo.payment.model.CardCrossReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardCrossReferenceRepository extends JpaRepository<CardCrossReference, String> {

    /**
     * Find card cross-reference by account ID.
     * Mirrors the COBOL READ on CXACAIX file using XREF-ACCT-ID as the key.
     */
    Optional<CardCrossReference> findByAccountId(Long accountId);
}
