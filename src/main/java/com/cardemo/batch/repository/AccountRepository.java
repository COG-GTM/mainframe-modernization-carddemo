package com.cardemo.batch.repository;

import com.cardemo.batch.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ACCTFILE (account master) records.
 * Equivalent to VSAM I-O access (READ + REWRITE) by ACCT-ID.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
}
