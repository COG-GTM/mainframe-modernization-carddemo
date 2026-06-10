package com.carddemo.interestcalc.repository;

import com.carddemo.interestcalc.domain.CardXrefRecord;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory stand-in for the XREF-FILE VSAM KSDS (keyed by account id alternate index). */
public class InMemoryCardXrefRepository implements CardXrefRepository {

    private final Map<Long, CardXrefRecord> byAccountId = new ConcurrentHashMap<>();

    public void load(CardXrefRecord xref) {
        byAccountId.put(xref.accountId(), xref);
    }

    @Override
    public Optional<CardXrefRecord> findByAccountId(long accountId) {
        return Optional.ofNullable(byAccountId.get(accountId));
    }
}
