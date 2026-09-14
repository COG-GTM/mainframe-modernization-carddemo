package com.carddemo.acctupdate.repository;

import com.carddemo.acctupdate.domain.CardXrefRecord;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CardXrefRepository extends JpaRepository<CardXrefRecord, String> {
    Optional<CardXrefRecord> findFirstByAcctId(Long acctId);
}
