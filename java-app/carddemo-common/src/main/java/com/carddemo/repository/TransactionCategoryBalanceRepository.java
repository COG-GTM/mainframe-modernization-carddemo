package com.carddemo.repository;

import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.entity.TransactionCategoryBalance.TransactionCategoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TransactionCategoryBalance entity.
 * Replaces indexed read on TCATBALF VSAM file.
 */
@Repository
public interface TransactionCategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceId> {

    @Query("SELECT tcb FROM TransactionCategoryBalance tcb WHERE tcb.id.acctId = :acctId ORDER BY tcb.id.acctId, tcb.id.tranTypeCd, tcb.id.tranCatCd")
    List<TransactionCategoryBalance> findByAcctIdOrdered(@Param("acctId") Long acctId);

    @Query("SELECT tcb FROM TransactionCategoryBalance tcb ORDER BY tcb.id.acctId, tcb.id.tranTypeCd, tcb.id.tranCatCd")
    List<TransactionCategoryBalance> findAllOrdered();
}
