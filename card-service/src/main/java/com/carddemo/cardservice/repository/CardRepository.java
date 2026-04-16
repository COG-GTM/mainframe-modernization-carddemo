package com.carddemo.cardservice.repository;

import com.carddemo.cardservice.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Card entity.
 * Maps to COBOL CARDDAT file (primary key access) and CARDAIX (alternate index by account).
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    /**
     * Find cards by account ID with pagination.
     * Equivalent to COBOL STARTBR on CARDAIX (alternate index by account).
     */
    Page<Card> findByCardAcctId(Long cardAcctId, Pageable pageable);
}
