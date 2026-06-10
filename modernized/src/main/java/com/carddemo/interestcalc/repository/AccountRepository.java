package com.carddemo.interestcalc.repository;

import com.carddemo.interestcalc.domain.AccountRecord;

import java.util.Optional;

/**
 * Keyed access to the account master, mirroring the VSAM KSDS {@code ACCOUNT-FILE}
 * (RECORD KEY {@code FD-ACCT-ID}) opened I-O by CBACT04C.
 */
public interface AccountRepository {

    /** Mirrors COBOL paragraph {@code 1100-GET-ACCT-DATA} (keyed READ). */
    Optional<AccountRecord> findById(long accountId);

    /** Mirrors the REWRITE in COBOL paragraph {@code 1050-UPDATE-ACCOUNT}. */
    void update(AccountRecord account);
}
