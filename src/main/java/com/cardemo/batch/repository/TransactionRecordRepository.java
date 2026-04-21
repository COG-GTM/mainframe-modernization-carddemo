package com.cardemo.batch.repository;

import com.cardemo.batch.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for TRANSACT (transaction output) records.
 * Equivalent to sequential WRITE to output VSAM file.
 */
@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, String> {
}
