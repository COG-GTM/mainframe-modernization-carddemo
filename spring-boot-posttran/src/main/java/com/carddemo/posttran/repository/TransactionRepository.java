package com.carddemo.posttran.repository;

import com.carddemo.posttran.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the transactions table (maps to TRANFILE / CVTRA05Y).
 * Posted transactions are written here.
 * Maps to COBOL section 2900-WRITE-TRANSACTION-FILE.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
}
