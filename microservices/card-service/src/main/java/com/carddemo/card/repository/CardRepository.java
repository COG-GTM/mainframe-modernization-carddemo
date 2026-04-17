package com.carddemo.card.repository;

import com.carddemo.card.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Card entity.
 * Translates VSAM KSDS CARDDATA file access (CICS READ/STARTBR/READNEXT)
 * from COCRDLIC.cbl, COCRDSLC.cbl, COCRDUPC.cbl, and CBACT02C.cbl.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String> {

    Page<Card> findByCardAcctId(String cardAcctId, Pageable pageable);
}
