package com.carddemo.repository;

import com.carddemo.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Account} ({@code ACCTFILE} KSDS).
 *
 * <p>CBACT04C reads the account randomly by {@code ACCT-ID} (paragraph {@code 1100-GET-ACCT-DATA})
 * and rewrites it (paragraph {@code 1050-UPDATE-ACCOUNT}); these map to
 * {@link JpaRepository#findById(Object)} and {@link JpaRepository#save(Object)}.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
