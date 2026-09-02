package com.carddemo.repository;

import com.carddemo.domain.DisclosureGroup;

import java.util.List;
import java.util.Optional;

/** DISCGRP: interest rates by account group, transaction type and category. */
public interface DisclosureGroupRepository {

    Optional<DisclosureGroup> find(String accountGroupId, String typeCode, int categoryCode);

    List<DisclosureGroup> findAll();
}
