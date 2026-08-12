package com.carddemo.repository;

import com.carddemo.model.entity.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Replaces sequential DALYTRAN file access used by the posting batch. */
@Repository
public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {
}
