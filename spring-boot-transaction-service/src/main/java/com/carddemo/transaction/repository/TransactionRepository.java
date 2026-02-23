package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.Transaction;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Transaction entity.
 * Replaces CICS file control commands for the TRANSACT VSAM file:
 * - EXEC CICS READ / WRITE / STARTBR / READPREV / ENDBR
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    /**
     * Find the transaction with the highest (last) ID.
     * Replaces the COBOL pattern: STARTBR at HIGH-VALUES then READPREV.
     */
    Optional<Transaction> findTopByOrderByTranIdDesc();

    /**
     * Find all transactions ordered by ID for paginated listing.
     * Replaces the COBOL browse (STARTBR/READNEXT) pattern in COTRN00C.
     */
    Page<Transaction> findAllByOrderByTranIdAsc(Pageable pageable);

    /**
     * Find transactions by card number.
     */
    Page<Transaction> findByTranCardNumOrderByTranIdAsc(String cardNum, Pageable pageable);
}
