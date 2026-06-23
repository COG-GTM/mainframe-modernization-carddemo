package com.carddemo.cardxref;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link CardXref}.
 *
 * <p>Mirrors the COBOL/VSAM access patterns for CARDXREF:</p>
 * <ul>
 *   <li>Keyed READ by {@code XREF-CARD-NUM} — {@link #findById} (VSAM RECORD KEY).</li>
 *   <li>Sequential READ of the whole file — {@link #findAll} (see {@code CBACT03C.cbl}).</li>
 *   <li>Lookup by account id — {@link #findByXrefAcctId} (xref queried by account).</li>
 *   <li>Lookup by customer id — {@link #findByXrefCustId} (xref queried by customer).</li>
 * </ul>
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    /** Cross-reference rows for the given account id, mirroring XREF-ACCT-ID lookups. */
    List<CardXref> findByXrefAcctId(Long xrefAcctId);

    /** Cross-reference rows for the given customer id, mirroring XREF-CUST-ID lookups. */
    List<CardXref> findByXrefCustId(Long xrefCustId);
}
