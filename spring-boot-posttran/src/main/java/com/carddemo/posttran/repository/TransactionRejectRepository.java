package com.carddemo.posttran.repository;

import com.carddemo.posttran.model.TransactionReject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the transaction_rejects table (maps to DALYREJS GDG reject file).
 * Rejected transactions are written here.
 * Maps to COBOL section 2500-WRITE-REJECT-REC.
 */
@Repository
public interface TransactionRejectRepository extends JpaRepository<TransactionReject, Long> {
}
