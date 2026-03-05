package com.carddemo.repository;

import com.carddemo.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Card entity.
 * Replaces CICS READ on CARDDAT VSAM file.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    List<Card> findByAcctId(Long acctId);
    Page<Card> findByAcctId(Long acctId, Pageable pageable);
}
