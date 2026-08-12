package com.carddemo.repository;

import com.carddemo.model.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Replaces VSAM KSDS ACCTDATA access (keyed on ACCT-ID). */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
