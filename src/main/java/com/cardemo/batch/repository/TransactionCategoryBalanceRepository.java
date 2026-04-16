package com.cardemo.batch.repository;

import com.cardemo.batch.model.TransactionCategoryBalance;
import com.cardemo.batch.model.TransactionCategoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionCategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceId> {

    @Query("SELECT t FROM TransactionCategoryBalance t ORDER BY t.acctId, t.typeCd, t.catCd")
    List<TransactionCategoryBalance> findAllOrderedByKey();
}
