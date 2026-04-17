package com.carddemo.account.repository;

import com.carddemo.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Account entity.
 * Replaces CICS READ/REWRITE operations on ACCTDAT VSAM file.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
}
