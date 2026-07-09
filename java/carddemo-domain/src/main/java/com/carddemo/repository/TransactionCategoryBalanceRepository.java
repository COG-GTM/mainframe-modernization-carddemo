package com.carddemo.repository;

import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.domain.TransactionCategoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionCategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceId> {
}
