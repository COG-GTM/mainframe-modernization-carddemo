package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Account entity.
 * Replaces CICS file control commands for the ACCTDAT VSAM file.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
