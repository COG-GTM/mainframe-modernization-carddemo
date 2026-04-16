package com.cardemo.batch.repository;

import com.cardemo.batch.model.DailyTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DailyTransactionEntity (DALYTRAN input file).
 */
@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransactionEntity, String> {
}
