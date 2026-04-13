package com.carddemo.entity;

import com.carddemo.repository.DisclosureGroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for DisclosureGroup entity confirming behavior of the underlying COBOL VSAM system.
 * Verifies seed data from discgrp.txt, CRUD operations, and composite key behavior.
 */
@DataJpaTest
class DisclosureGroupTest {

    @Autowired
    private DisclosureGroupRepository repository;

    @Test
    void seedDataLoadsExpectedNumberOfDisclosureGroups() {
        List<DisclosureGroup> all = repository.findAll();
        assertThat(all).hasSize(51);
    }

    @Test
    void firstDisclosureGroupFieldsMatchSeedData() {
        List<DisclosureGroup> all = repository.findAll();
        assertThat(all).isNotEmpty();
        DisclosureGroup first = all.get(0);
        assertThat(first.getDisAcctGroupId()).isNotNull();
        assertThat(first.getDisTranTypeCd()).isNotNull();
        assertThat(first.getDisTranCatCd()).isGreaterThanOrEqualTo(0);
        assertThat(first.getDisIntRate()).isNotNull();
    }

    @Test
    void findByAcctGroupIdReturnsMatchingGroups() {
        List<DisclosureGroup> all = repository.findAll();
        String groupId = all.get(0).getDisAcctGroupId().trim();
        List<DisclosureGroup> results = repository.findByDisAcctGroupId(groupId);
        assertThat(results).isNotEmpty();
        results.forEach(dg -> assertThat(dg.getDisAcctGroupId().trim()).isEqualTo(groupId));
    }

    @Test
    void createDisclosureGroupPersistsAndReadsBack() {
        DisclosureGroup dg = new DisclosureGroup();
        dg.setDisAcctGroupId("TESTGRP");
        dg.setDisTranTypeCd("ZZ");
        dg.setDisTranCatCd(9999);
        dg.setDisIntRate(new BigDecimal("15.50"));

        repository.save(dg);

        DisclosureGroup.DisclosureGroupId id = new DisclosureGroup.DisclosureGroupId();
        id.setDisAcctGroupId("TESTGRP");
        id.setDisTranTypeCd("ZZ");
        id.setDisTranCatCd(9999);

        assertThat(repository.findById(id)).isPresent();
        assertThat(repository.findById(id).get().getDisIntRate())
                .isEqualByComparingTo(new BigDecimal("15.50"));
    }

    @Test
    void updateInterestRate() {
        List<DisclosureGroup> all = repository.findAll();
        DisclosureGroup dg = all.get(0);
        dg.setDisIntRate(new BigDecimal("25.99"));
        repository.save(dg);

        DisclosureGroup.DisclosureGroupId id = new DisclosureGroup.DisclosureGroupId();
        id.setDisAcctGroupId(dg.getDisAcctGroupId());
        id.setDisTranTypeCd(dg.getDisTranTypeCd());
        id.setDisTranCatCd(dg.getDisTranCatCd());

        DisclosureGroup updated = repository.findById(id).orElseThrow();
        assertThat(updated.getDisIntRate()).isEqualByComparingTo(new BigDecimal("25.99"));
    }

    @Test
    void deleteDisclosureGroupRemovesFromDatabase() {
        List<DisclosureGroup> before = repository.findAll();
        int beforeSize = before.size();
        DisclosureGroup dg = before.get(0);

        DisclosureGroup.DisclosureGroupId id = new DisclosureGroup.DisclosureGroupId();
        id.setDisAcctGroupId(dg.getDisAcctGroupId());
        id.setDisTranTypeCd(dg.getDisTranTypeCd());
        id.setDisTranCatCd(dg.getDisTranCatCd());

        repository.deleteById(id);
        assertThat(repository.findAll()).hasSize(beforeSize - 1);
    }

    @Test
    void compositeKeyIsUniqueAcrossAllRecords() {
        List<DisclosureGroup> all = repository.findAll();
        long distinctKeys = all.stream()
                .map(dg -> dg.getDisAcctGroupId() + "|" + dg.getDisTranTypeCd() + "|" + dg.getDisTranCatCd())
                .distinct().count();
        assertThat(distinctKeys).isEqualTo(all.size());
    }

    @Test
    void allDisclosureGroupsHaveInterestRates() {
        List<DisclosureGroup> all = repository.findAll();
        all.forEach(dg -> assertThat(dg.getDisIntRate()).isNotNull());
    }
}
