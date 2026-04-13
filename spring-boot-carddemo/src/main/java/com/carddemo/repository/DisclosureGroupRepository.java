package com.carddemo.repository;

import com.carddemo.entity.DisclosureGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroup.DisclosureGroupId> {

    List<DisclosureGroup> findByDisAcctGroupId(String groupId);
}
