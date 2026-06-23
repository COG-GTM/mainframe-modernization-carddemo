package com.carddemo.account;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Account}.
 *
 * <p>Mirrors the COBOL/VSAM access patterns for ACCTDAT:</p>
 * <ul>
 *   <li>Keyed READ by {@code ACCT-ID} — {@link #findById} (see {@code COACTVWC.cbl}).</li>
 *   <li>Sequential READ of the whole file — {@link #findAll} (see {@code CBACT01C.cbl}).</li>
 *   <li>Filter by status / group id — finder methods below.</li>
 * </ul>
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /** Accounts with the given active-status flag (Y/N), mirroring ACCT-ACTIVE-STATUS scans. */
    List<Account> findByAcctActiveStatus(String acctActiveStatus);

    /** Accounts belonging to the given group id, mirroring ACCT-GROUP-ID grouping. */
    List<Account> findByAcctGroupId(String acctGroupId);
}
