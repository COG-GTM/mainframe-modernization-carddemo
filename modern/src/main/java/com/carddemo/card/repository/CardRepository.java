package com.carddemo.card.repository;

import com.carddemo.card.entity.CardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for card data.
 *
 * Migrated from: COCRDLIC.cbl browse operations (STARTBR/READNEXT/READPREV on CARDDAT)
 * Provides pagination support replacing CICS browse with Spring Pageable.
 */
@Repository
public interface CardRepository extends JpaRepository<CardEntity, String> {

    /**
     * Find cards by account ID with pagination.
     * Replaces COCRDLIC.cbl filtering by account within CARDDAT browse.
     */
    Page<CardEntity> findByAccountId(Long accountId, Pageable pageable);
}
