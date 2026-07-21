package com.carddemo.billpay.repository;

import com.carddemo.billpay.domain.CardXrefEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** CXACAIX AIX read by account id (COBIL00C: READ-CXACAIX-FILE, RIDFLD XREF-ACCT-ID). */
public interface CardXrefRepository extends JpaRepository<CardXrefEntity, String> {
    Optional<CardXrefEntity> findFirstByAcctIdOrderByCardNum(Long acctId);
}
