package com.carddemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.carddemo.model.Account;

/**
 * Repository for account master data.
 *
 * Replaces: ACCOUNT-FILE I-O operations in CBTRN02C:
 *   - READ by FD-ACCT-ID (1500-B-LOOKUP-ACCT for validation)
 *   - REWRITE (2800-UPDATE-ACCOUNT-REC for balance updates)
 *
 * Also replaces: ACCTFILE READ in CBSTM03A (3000-ACCTFILE-GET)
 * and CBACT04C (interest calculation account updates).
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
