package com.carddemo.repository;

import com.carddemo.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Transaction entity.
 * Replaces CICS READ/STARTBR/READNEXT on TRANSACT VSAM file.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Page<Transaction> findByCardNum(String cardNum, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.cardNum IN " +
           "(SELECT cx.cardNum FROM CardXref cx WHERE cx.acctId = :acctId)")
    Page<Transaction> findByAcctId(@Param("acctId") Long acctId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.origTimestamp BETWEEN :startDate AND :endDate")
    List<Transaction> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t FROM Transaction t WHERE t.cardNum IN " +
           "(SELECT cx.cardNum FROM CardXref cx WHERE cx.acctId = :acctId) " +
           "AND t.origTimestamp BETWEEN :startDate AND :endDate")
    List<Transaction> findByAcctIdAndDateRange(
        @Param("acctId") Long acctId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
