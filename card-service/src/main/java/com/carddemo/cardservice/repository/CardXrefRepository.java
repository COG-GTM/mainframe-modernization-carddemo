package com.carddemo.cardservice.repository;

import com.carddemo.cardservice.entity.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CardXref entity.
 * Maps to COBOL CARDXREF cross-reference file.
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findByXrefAcctId(Long acctId);

    List<CardXref> findByXrefCustId(Long custId);
}
