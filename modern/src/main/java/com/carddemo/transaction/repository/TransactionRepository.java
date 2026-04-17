package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for transaction data access.
 *
 * COBOL Traceability: Replaces EXEC CICS READ/WRITE/STARTBR/READNEXT/READPREV
 * operations on the TRANSACT VSAM KSDS file used by COTRN00C, COTRN01C,
 * COTRN02C, and COBIL00C.
 */
@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

    /**
     * Find transactions with optional filters.
     * Replaces COTRN00C's STARTBR/READNEXT browse with filtering.
     */
    @Query("SELECT t FROM TransactionEntity t WHERE "
            + "(:accountId IS NULL OR t.cardNumber IN "
            + "  (SELECT x.cardNumber FROM CardXrefEntity x WHERE x.accountId = :accountId)) "
            + "AND (:cardNumber IS NULL OR t.cardNumber = :cardNumber) "
            + "AND (:startDate IS NULL OR t.originTimestamp >= :startDate) "
            + "AND (:endDate IS NULL OR t.originTimestamp <= :endDate) "
            + "ORDER BY t.transactionId DESC")
    Page<TransactionEntity> findWithFilters(
            @Param("accountId") String accountId,
            @Param("cardNumber") String cardNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Atomically generate the next transaction ID from the database sequence.
     * Replaces COTRN02C's STARTBR with HIGH-VALUES / READPREV to get last ID.
     * Uses a DB sequence to avoid race conditions under concurrent requests.
     */
    @Query(value = "SELECT NEXT VALUE FOR transaction_id_seq", nativeQuery = true)
    long nextTransactionIdFromSequence();
}
