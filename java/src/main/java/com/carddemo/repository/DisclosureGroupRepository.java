package com.carddemo.repository;

import com.carddemo.model.entity.DisclosureGroup;
import com.carddemo.model.entity.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Replaces VSAM KSDS DISCGRP access (keyed on DIS-GROUP-KEY). */
@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {
}
