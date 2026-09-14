package com.carddemo.acctupdate.repository;

import com.carddemo.acctupdate.domain.CardXrefRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CardXrefRepository extends JpaRepository<CardXrefRecord, String> {
    Optional<CardXrefRecord> findFirstByAcctId(Long acctId);
}
