package com.carddemo.repository;

import com.carddemo.entity.DisclosureGroup;
import com.carddemo.entity.DisclosureGroup.DisclosureGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for DisclosureGroup entity.
 * Replaces indexed read on DISCGRP VSAM file.
 */
@Repository
public interface DisclosureGroupRepository extends JpaRepository<DisclosureGroup, DisclosureGroupId> {

    @Query("SELECT dg FROM DisclosureGroup dg WHERE dg.id.groupId = :groupId AND dg.id.tranTypeCd = :tranTypeCd AND dg.id.tranCatCd = :tranCatCd")
    Optional<DisclosureGroup> findByGroupIdAndTranTypeCdAndTranCatCd(
        @Param("groupId") String groupId,
        @Param("tranTypeCd") String tranTypeCd,
        @Param("tranCatCd") Integer tranCatCd
    );

    /**
     * Fallback query to find the DEFAULT group when the account's specific group is not found.
     * Mirrors COBOL logic: IF DISCGRP-STATUS = '23' (record not found), try with 'DEFAULT'.
     */
    @Query("SELECT dg FROM DisclosureGroup dg WHERE dg.id.groupId = 'DEFAULT' AND dg.id.tranTypeCd = :tranTypeCd AND dg.id.tranCatCd = :tranCatCd")
    Optional<DisclosureGroup> findDefaultGroup(
        @Param("tranTypeCd") String tranTypeCd,
        @Param("tranCatCd") Integer tranCatCd
    );
}
