package com.carddemo.interestcalc.repository;

import com.carddemo.interestcalc.domain.TransactionRecord;

/**
 * Sequential output of interest transactions, mirroring the sequential
 * {@code TRANSACT-FILE} opened OUTPUT by CBACT04C.
 */
public interface TransactionWriter {

    /** Mirrors the WRITE in COBOL paragraph {@code 1300-B-WRITE-TX}. */
    void write(TransactionRecord transaction);
}
