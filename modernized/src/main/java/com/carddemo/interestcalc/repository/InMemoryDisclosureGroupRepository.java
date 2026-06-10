package com.carddemo.interestcalc.repository;

import com.carddemo.interestcalc.domain.DisclosureGroupRecord;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory stand-in for the DISCGRP-FILE VSAM KSDS. */
public class InMemoryDisclosureGroupRepository implements DisclosureGroupRepository {

    private final Map<String, DisclosureGroupRecord> byKey = new ConcurrentHashMap<>();

    public void load(DisclosureGroupRecord record) {
        byKey.put(key(record.accountGroupId(), record.transactionTypeCode(), record.transactionCategoryCode()), record);
    }

    @Override
    public Optional<DisclosureGroupRecord> findByKey(String accountGroupId, String transactionTypeCode, int transactionCategoryCode) {
        return Optional.ofNullable(byKey.get(key(accountGroupId, transactionTypeCode, transactionCategoryCode)));
    }

    private static String key(String groupId, String typeCode, int categoryCode) {
        return groupId + "|" + typeCode + "|" + categoryCode;
    }
}
