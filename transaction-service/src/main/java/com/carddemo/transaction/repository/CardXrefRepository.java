package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for CardXref entity.
 * Replaces CICS READ on CXACAIX (by account) and CCXREF (by card) VSAM files.
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    Optional<CardXref> findByXrefAcctId(Long acctId);

    Optional<CardXref> findByXrefCardNum(String cardNum);
}
