package com.carddemo.repository;

import com.carddemo.model.CardXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link CardXref} ({@code XREFFILE} KSDS).
 *
 * <p>CBACT04C looks up the xref by account id via the alternate index {@code CXACAIX} (paragraph
 * {@code 1110-GET-XREF-DATA}); {@link #findFirstByAcctId(Long)} reproduces that AIX read, returning
 * the first matching card (the COBOL random read returns one record for the key).
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    /** Alternate-index (CXACAIX) lookup by account id. */
    Optional<CardXref> findFirstByAcctId(Long acctId);

    /** All cards cross-referenced to an account (full AIX path). */
    List<CardXref> findByAcctId(Long acctId);
}
