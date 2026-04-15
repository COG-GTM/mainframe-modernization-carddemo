package com.carddemo.card.repository;

import com.carddemo.card.model.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Card entities.
 * Mirrors the VSAM KSDS file access patterns used in the COBOL programs:
 * - CARDDAT file: keyed by card number (primary key)
 * - CARDAIX file: alternate index by account ID (findByAccountId)
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    /**
     * Find all cards for a given account ID with pagination.
     * Equivalent to COBOL STARTBR on CARDAIX (alternate index by account).
     */
    Page<Card> findByAccountId(String accountId, Pageable pageable);
}
