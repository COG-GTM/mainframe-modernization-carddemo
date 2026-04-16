package com.cardemo.batch.repository;

import com.cardemo.batch.model.DisclosureGroup;
import com.cardemo.batch.model.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for DISCGRP (disclosure group) records.
 * Equivalent to VSAM random-access READ by composite key.
 */
@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {

    Optional<DisclosureGroup> findByAcctGroupIdAndTranTypeCdAndTranCatCd(
            String acctGroupId, String tranTypeCd, String tranCatCd);
}
