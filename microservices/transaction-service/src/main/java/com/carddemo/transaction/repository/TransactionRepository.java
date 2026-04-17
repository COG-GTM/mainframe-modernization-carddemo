package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Transaction entity.
 * Translates COBOL VSAM KSDS file access (STARTBR/READNEXT/READ) to JPA queries.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findByTranCardNum(String tranCardNum, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE "
            + "(:cardNum IS NULL OR t.tranCardNum = :cardNum) AND "
            + "(:startDate IS NULL OR t.tranOrigTs >= :startDate) AND "
            + "(:endDate IS NULL OR t.tranOrigTs <= :endDate)")
    Page<Transaction> findWithFilters(
            @Param("cardNum") String cardNum,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            Pageable pageable);
}
