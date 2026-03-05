package com.carddemo.repository;

import com.carddemo.entity.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DailyTransaction entity (staging table).
 * Replaces sequential read of DALYTRAN VSAM file.
 */
@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {
}
