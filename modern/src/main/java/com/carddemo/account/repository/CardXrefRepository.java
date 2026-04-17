package com.carddemo.account.repository;

import com.carddemo.account.entity.CardXrefEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for CXACAIX (Card-to-Account Cross-Reference AIX) access.
 * Migrated from: EXEC CICS STARTBR/READNEXT FILE('CXACAIX') in COACTVWC.cbl / COACTUPC.cbl
 */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXrefEntity, String> {

    List<CardXrefEntity> findByAccountId(Long accountId);
}
