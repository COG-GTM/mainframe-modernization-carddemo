package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.Transaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Transaction entity.
 *
 * Replaces CICS file control commands on the TRANSACT VSAM file:
 *   - STARTBR / READPREV / ENDBR (browsing for max ID)
 *   - WRITE (adding new record)
 *   - READ (reading by key)
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find the most recent transaction (highest ID).
     * Replaces the COBOL pattern: STARTBR with HIGH-VALUES, READPREV, ENDBR.
     */
    Optional<Transaction> findFirstByOrderByTransactionIdDesc();
}
