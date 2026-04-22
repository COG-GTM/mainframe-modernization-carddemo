package com.carddemo.report.repository;

import com.carddemo.report.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByTranProcTsBetweenOrderByTranCardNumAscTranProcTsAsc(
            LocalDateTime startDate, LocalDateTime endDate);

    List<Transaction> findByAccountIdOrderByTranProcTsAsc(String accountId);

    List<Transaction> findByTranCardNumOrderByTranProcTsAsc(String cardNumber);

    List<Transaction> findByAccountIdAndTranProcTsBetweenOrderByTranProcTsAsc(
            String accountId, LocalDateTime startDate, LocalDateTime endDate);
}
