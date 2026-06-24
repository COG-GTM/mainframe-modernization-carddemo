package com.carddemo.account.repository;

import com.carddemo.account.model.Account;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Modern equivalent of CICS/batch file control over the {@code ACCTDAT} VSAM
 * KSDS. The KSDS is keyed and read sequentially by {@code FD-ACCT-ID}, so the
 * relational mapping is a table with {@code acct_id} as primary key; ordered
 * iteration ({@link #findAllByOrderByAcctIdAsc()}) reproduces the sequential
 * read order CBACT01C relies on.
 */
public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findAllByOrderByAcctIdAsc();
}
