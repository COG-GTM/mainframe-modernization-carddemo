package com.cardemo.batch.repository;

import com.cardemo.batch.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ACCTFILE — account records.
 * Replaces CBSTM03B keyed reads (READ-K) on ACCT-FILE.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
}
