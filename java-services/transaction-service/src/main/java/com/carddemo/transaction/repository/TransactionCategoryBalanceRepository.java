package com.carddemo.transaction.repository;

import com.carddemo.transaction.model.TransactionCategoryBalance;
import com.carddemo.transaction.model.TransactionCategoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionCategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceId> {
}
