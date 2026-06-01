package com.carddemo.repository;

import com.carddemo.model.DisclosureGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link DisclosureGroup} ({@code DISCGRP} KSDS).
 *
 * <p>CBACT04C reads this file randomly by the composite key (paragraph
 * {@code 1200-GET-INTEREST-RATE}); a missing key (file status {@code '23'}) triggers a retry with
 * the {@code 'DEFAULT'} group (paragraph {@code 1200-A-GET-DEFAULT-INT-RATE}). Both reads use
 * {@link JpaRepository#findById(Object)} with a {@link DisclosureGroup.DisclosureGroupId}.
 */
@Repository
public interface DisclosureGroupRepository
        extends JpaRepository<DisclosureGroup, DisclosureGroup.DisclosureGroupId> {
}
