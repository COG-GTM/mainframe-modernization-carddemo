package com.carddemo.interestcalc.repository;

import com.carddemo.interestcalc.domain.CardXrefRecord;

import java.util.Optional;

/**
 * Access to the card cross-reference file, mirroring the VSAM KSDS {@code XREF-FILE}
 * read via ALTERNATE RECORD KEY {@code FD-XREF-ACCT-ID} by CBACT04C.
 */
public interface CardXrefRepository {

    /** Mirrors COBOL paragraph {@code 1110-GET-XREF-DATA} (READ ... KEY IS FD-XREF-ACCT-ID). */
    Optional<CardXrefRecord> findByAccountId(long accountId);
}
