package com.carddemo.account.repository;

import com.carddemo.account.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ACCTDAT (Account Master) access.
 * Migrated from: EXEC CICS READ FILE('ACCTDAT') in COACTVWC.cbl / COACTUPC.cbl
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
}
