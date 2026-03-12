package com.carddemo.posttran.repository;

import com.carddemo.posttran.model.TransactionCategoryBalance;
import com.carddemo.posttran.model.TransactionCategoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * Repository for the transaction_category_balances table (maps to TCATBALF / CVTRA01Y).
 * Provides upsert semantics for category balance records.
 * Maps to COBOL section 2700-UPDATE-TCATBAL.
 */
@Repository
public interface CategoryBalanceRepository
        extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceId> {

    /**
     * Update the balance for an existing category balance record.
     * Maps to COBOL 2700-B-UPDATE-TCATBAL-REC: ADD DALYTRAN-AMT TO TRAN-CAT-BAL.
     */
    @Modifying
    @Query("UPDATE TransactionCategoryBalance t SET " +
           "t.tranCatBal = t.tranCatBal + :amount " +
           "WHERE t.trancatAcctId = :acctId AND t.trancatTypeCd = :typeCd AND t.trancatCd = :catCd")
    int updateBalance(@Param("acctId") long acctId,
                      @Param("typeCd") String typeCd,
                      @Param("catCd") int catCd,
                      @Param("amount") BigDecimal amount);
}
