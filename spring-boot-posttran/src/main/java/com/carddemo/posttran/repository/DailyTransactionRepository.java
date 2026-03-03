package com.carddemo.posttran.repository;

import com.carddemo.posttran.model.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the daily_transactions staging table (maps to DALYTRAN file).
 * Used by the ItemReader to read daily transaction records for processing.
 */
@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {
}
