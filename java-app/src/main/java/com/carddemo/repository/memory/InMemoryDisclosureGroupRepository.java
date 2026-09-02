package com.carddemo.repository.memory;

import com.carddemo.domain.DisclosureGroup;
import com.carddemo.fixture.CardDemoDataSet;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.util.CobolCodec;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** DISCGRP interest rate table seeded from discgrp.txt. */
@Repository
public class InMemoryDisclosureGroupRepository implements DisclosureGroupRepository, Reseedable {

    private final CardDemoDataSet dataSet;
    private final Map<String, DisclosureGroup> groups = new LinkedHashMap<>();

    public InMemoryDisclosureGroupRepository(CardDemoDataSet dataSet) {
        this.dataSet = dataSet;
        seed();
    }

    @Override
    public void reseed() {
        seed();
    }

    private void seed() {
        groups.clear();
        dataSet.disclosureGroups().forEach(group -> groups.put(group.key(), group));
    }

    @Override
    public Optional<DisclosureGroup> find(String accountGroupId, String typeCode, int categoryCode) {
        return Optional.ofNullable(groups.get(CobolCodec.encodeText(accountGroupId, 10)
                + CobolCodec.encodeText(typeCode, 2) + CobolCodec.encodeNumeric(categoryCode, 4)));
    }

    @Override
    public List<DisclosureGroup> findAll() {
        return new ArrayList<>(groups.values());
    }
}
