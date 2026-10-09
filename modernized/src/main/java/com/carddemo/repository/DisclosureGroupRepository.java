package com.carddemo.repository;

import com.carddemo.domain.DisclosureGroup;
import com.carddemo.domain.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {
}
