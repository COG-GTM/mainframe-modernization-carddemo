package com.carddemo.repository;

import com.carddemo.entity.DisclosureGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroup.DisclosureGroupKey> {
    Optional<DisclosureGroup> findByAcctGroupIdAndTranTypeCdAndTranCatCd(String groupId, String typeCd, Integer catCd);
}
