package com.cardemo.repository;

import com.cardemo.entity.DisclosureGroup;
import com.cardemo.entity.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link DisclosureGroup}.
 * Reference data: interest rates per account group and transaction type/category.
 */
@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {

    List<DisclosureGroup> findByDisAcctGroupId(String acctGroupId);
}
