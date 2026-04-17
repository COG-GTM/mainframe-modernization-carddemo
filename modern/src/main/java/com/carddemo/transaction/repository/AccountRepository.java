package com.carddemo.transaction.repository;

import com.carddemo.transaction.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for account data access.
 *
 * COBOL Traceability: Replaces EXEC CICS READ/REWRITE on ACCTDAT VSAM file
 * used by COBIL00C for bill payment (read balance, update after payment).
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, String> {
}
