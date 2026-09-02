package com.carddemo.repository;

import com.carddemo.domain.CardXref;

import java.util.List;
import java.util.Optional;

/** CARDXREF (VSAM KSDS keyed on XREF-CARD-NUM, alternate index on XREF-ACCT-ID). */
public interface CardXrefRepository {

    Optional<CardXref> findByCardNumber(String cardNumber);

    List<CardXref> findByAccountId(String accountId);

    List<CardXref> findByCustomerId(String customerId);

    List<CardXref> findAll();

    CardXref save(CardXref xref);
}
