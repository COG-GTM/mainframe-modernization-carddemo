package com.carddemo.interestcalc.repository;

import com.carddemo.interestcalc.domain.DisclosureGroupRecord;

import java.util.Optional;

/**
 * Keyed access to the disclosure group file, mirroring the VSAM KSDS {@code DISCGRP-FILE}
 * (RECORD KEY {@code FD-DISCGRP-KEY} = group id + tran type + tran category).
 */
public interface DisclosureGroupRepository {

    /** Mirrors the keyed READ in COBOL paragraphs {@code 1200-GET-INTEREST-RATE} / {@code 1200-A-GET-DEFAULT-INT-RATE}. */
    Optional<DisclosureGroupRecord> findByKey(String accountGroupId, String transactionTypeCode, int transactionCategoryCode);
}
