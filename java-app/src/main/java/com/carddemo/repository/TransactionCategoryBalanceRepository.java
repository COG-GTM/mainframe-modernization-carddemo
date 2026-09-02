package com.carddemo.repository;

import com.carddemo.domain.TransactionCategoryBalance;

import java.util.List;
import java.util.Optional;

/** TCATBALF: per account / type / category running balances updated by the batch jobs. */
public interface TransactionCategoryBalanceRepository {

    Optional<TransactionCategoryBalance> find(String accountId, String typeCode, int categoryCode);

    List<TransactionCategoryBalance> findByAccountId(String accountId);

    List<TransactionCategoryBalance> findAll();

    TransactionCategoryBalance save(TransactionCategoryBalance balance);
}
