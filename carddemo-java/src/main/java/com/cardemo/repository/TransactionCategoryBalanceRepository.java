package com.cardemo.repository;

import com.cardemo.entity.TransactionCategoryBalance;
import com.cardemo.entity.TransactionCategoryBalanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link TransactionCategoryBalance}.
 * Tracks balances per account per transaction type/category.
 * Programs: CBACT04C (batch interest calc), CBTRN02C (batch post).
 */
@Repository
public interface TransactionCategoryBalanceRepository extends JpaRepository<TransactionCategoryBalance, TransactionCategoryBalanceId> {

    List<TransactionCategoryBalance> findByTrancatAcctId(Long acctId);
}
