package com.carddemo.repository;

import com.carddemo.entity.DailyTransactionReject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for DailyTransactionReject entity.
 * Replaces DALYREJS sequential output file.
 */
@Repository
public interface DailyTransactionRejectRepository extends JpaRepository<DailyTransactionReject, Long> {
}
