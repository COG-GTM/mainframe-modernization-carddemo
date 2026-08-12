package com.carddemo.repository;

import com.carddemo.model.entity.CardXref;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Replaces VSAM KSDS CARDXREF access (keyed on XREF-CARD-NUM, AIX on XREF-ACCT-ID). */
@Repository
public interface CardXrefRepository extends JpaRepository<CardXref, String> {

    List<CardXref> findByAccountIdOrderByCardNumber(Long accountId);

    Optional<CardXref> findFirstByAccountId(Long accountId);

    List<CardXref> findByCustomerIdOrderByCardNumber(Long customerId);
}
