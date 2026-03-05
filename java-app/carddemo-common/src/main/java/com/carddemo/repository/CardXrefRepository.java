package com.carddemo.repository;

import com.carddemo.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CardXref entity.
 * Replaces CICS READ/STARTBR/READNEXT on CARDXREF VSAM file.
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {
    List<CardXref> findByAcctId(Long acctId);
    List<CardXref> findByCustId(Long custId);
}
